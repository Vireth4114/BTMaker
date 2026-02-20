package BTMaker.BTMaker.resources

import javafx.scene.Node
import javafx.scene.image.Image
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.util.*
import java.util.jar.JarFile

object ResourceManager {
    const val SPRITE_SCALE = 65536.0 / 43266.0
    private var batchAtOffset = HashMap<Int, List<Resource>>()
    val spritesheets = HashMap<String, Image>()
    val singleSpriteMetadata = HashMap<Short, SpriteMetadata>()
    val compoundSpriteMetadata = HashMap<Short, List<SubSpriteMetadata>>()
    val animatedSpriteFrames = HashMap<Short, List<Short?>>()

    @JvmStatic
    fun main(args: Array<String>) {
        val jarFile = JarFile("""C:\Users\rapha\Documents\KEmulator\bouncetales.jar""")
        loadResourcesFrom(jarFile)
    }

    fun loadResourcesFrom(jarFile: JarFile) {
        loadBatchMapping(jarFile)
        loadSpriteBatches(jarFile)
    }

    fun loadBatchMapping(jarFile: JarFile) = getResourceFileInputStream(jarFile).apply {
        val resources = List(readShort().toInt()) {
            Resource(
                path = readUTF(),
                offset = readInt(),
                length = readInt()
            )
        }
        val batchCount = readShort().toInt()
        repeat(batchCount) {
            val type = ResourceType.fromCode(readByte())
            val resourceCount = readByte().toInt()
            val batchData = resources[readShort().toInt()].apply {
                if
                this.type = type
            }

            val resourcesInBatch = List(resourceCount) {
                resources[readShort().toInt()]
            }.onEach { it.type = type }

            if (type == ResourceType.IMAGE) {
                batchAtOffset[batchData.offset] = resourcesInBatch
            }
        }

        for (resource in resources) {
            println("Resource: ${resource.path}, offset: ${resource.offset}, length: ${resource.length}, type: ${resource.type}")
        }

        resources.filter { it.type == ResourceType.IMAGE }.forEach { png ->
            spritesheets[png.path] = Image(png.getInputStream(jarFile))
        }
    }

    fun loadSpriteBatches(jarFile: JarFile) {
        getSpriteDataFileInputStream(jarFile).apply {
            for ((offset, spritesheetsInBatch) in batchAtOffset) {
                skip(offset.toLong())
                loadSpriteBatch(this, spritesheetsInBatch)
            }
        }
    }

    fun loadSpriteBatch(stream: DataInputStream, spritesheetsInBatch: List<Resource>) = stream.apply {
        readByte() /* Spritesheet count, useless here */
        val metadataLoadingContext = MetadataLoadingContext(
            spritesheets = spritesheetsInBatch,
            baseSpritesheetId = readShort(),
            is16Bit = false
        )
        val spriteCount = readShort().toInt()
        val complexSpritesDataLength = readShort()
        val imageMapCount = readShort().toInt()
        val complexSpritesDataOffsets = buildMap {
            repeat(spriteCount) {
                put(readShort(), readShort())
            }
        }
        val complexSpritesData = readNBytes(complexSpritesDataLength.toInt())

        repeat (imageMapCount) {
            registerSpriteMetadata(
                stream = this,
                spriteID = readShort(),
                type = SpriteType.SIMPLE,
                metadataLoadingContext
            )
        }

        loadComplexSprites(complexSpritesData, complexSpritesDataOffsets, metadataLoadingContext)
    }

    fun loadComplexSprites(
        complexSpritesData: ByteArray,
        complexSpritesDataOffsets: Map<Short, Short>,
        metadataLoadingContext: MetadataLoadingContext
    ) {
        for ((spriteID, offset) in complexSpritesDataOffsets) {
            DataInputStream(ByteArrayInputStream(complexSpritesData)).apply {
                skip(offset.toLong())

                val flags = readByte().toInt()
                metadataLoadingContext.is16Bit = (flags and 4) != 0
                val spriteType = SpriteType.fromCode(flags % 4)

                registerSpriteMetadata(this, spriteID, spriteType, metadataLoadingContext)
            }
        }
    }

    fun registerSpriteMetadata(
        stream: DataInputStream,
        spriteID: Short,
        type: SpriteType,
        metadataLoadingContext: MetadataLoadingContext
    ) {
        when (type) {
            SpriteType.SIMPLE -> {
                singleSpriteMetadata[spriteID] = SpriteMetadata.readFromStream(stream, metadataLoadingContext)
            }

            SpriteType.COMPOUND -> {
                val is16bit = metadataLoadingContext.is16Bit
                stream.skip(if (is16bit) 8 else 4) /* 4 Unknown values */
                compoundSpriteMetadata[spriteID] = List(stream.readShort().toInt()) {
                    SubSpriteMetadata.readFromStream(stream, is16bit)
                }
            }

            SpriteType.ANIMATED -> {
                animatedSpriteFrames[spriteID] = List(stream.readShort().toInt()) {
                    stream.readShort()
                }
            }
        }
    }

    // TODO: cache
    fun getSpriteById(id: Short): Node {
        singleSpriteMetadata[id]?.let { return SimpleSprite(it) }
        compoundSpriteMetadata[id]?.let { return CompoundSprite(it) }
        animatedSpriteFrames[id]?.firstOrNull()?.let { return getSpriteById(it) } // TODO: Implement animated sprites properly instead of just returning the first frame

        throw IllegalArgumentException("No sprite found with ID: $id")
    }


    fun getResourceFileInputStream(jarFile: JarFile): DataInputStream {
        val entry = jarFile.getJarEntry("a")
            ?: throw IllegalArgumentException("Could not find resource file (/a)")
        return DataInputStream(jarFile.getInputStream(entry))
    }

    fun getSpriteDataFileInputStream(jarFile: JarFile): DataInputStream {
        val entry = jarFile.getJarEntry("b")
            ?: throw IllegalArgumentException("Could not find sprite data file (/b)")
        return DataInputStream(jarFile.getInputStream(entry))
    }
}