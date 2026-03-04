package BTMaker.BTMaker.resources

import javafx.scene.Node
import javafx.scene.image.Image
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.util.jar.JarFile

object ResourceManager {
    private var batchAtOffset = HashMap<Int, List<Resource>>()
    val spritesheets = HashMap<String, Image>()
    private val compoundSpritesMetadata = HashMap<Short, List<SubSpriteMetadata>>()
    private val animatedSpriteFrames = HashMap<Short, List<Short>>()
    val sprites = HashMap<Short, Node>()

    @JvmStatic
    fun main(args: Array<String>) {
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
            val batchData = resources[readShort().toInt()]

            val resourcesInBatch = List(resourceCount) {
                resources[readShort().toInt()]
            }.onEach {
                if (type == ResourceType.IMAGE && !spritesheets.containsKey(it.path)) {
                    spritesheets[it.path] = Image(it.getInputStream(jarFile))
                }
            }

            if (type == ResourceType.IMAGE) {
                batchAtOffset[batchData.offset] = resourcesInBatch
            }
        }
    }

    fun loadSpriteBatches(jarFile: JarFile) {
        for ((offset, spritesheetsInBatch) in batchAtOffset) {
            getSpriteDataFileInputStream(jarFile).apply {
                skip(offset.toLong())
                loadSpriteBatch(this, spritesheetsInBatch)
            }
        }

        for ((id, subSprites) in compoundSpritesMetadata) {
            sprites[id] = CompoundSprite(subSprites)
        }

        for ((id, frameIds) in animatedSpriteFrames) {
            sprites[id] = AnimatedSprite(frameIds.map { sprites[it]!! })
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
                sprites[spriteID] = SimpleSprite(SpriteMetadata.readFromStream(stream, metadataLoadingContext))
            }

            SpriteType.COMPOUND -> {
                val is16bit = metadataLoadingContext.is16Bit
                stream.skip(if (is16bit) 8 else 4) /* 4 Unknown values */
                compoundSpritesMetadata[spriteID] = List(stream.readShort().toInt()) {
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