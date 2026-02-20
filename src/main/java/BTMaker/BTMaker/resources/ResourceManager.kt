package BTMaker.BTMaker.resources

import javafx.scene.Node
import javafx.scene.image.Image
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.IOException
import java.util.*
import java.util.jar.JarFile

object ResourceManager {
    const val SPRITE_SCALE = 65536.0 / 43266.0
    private var imagesAtOffset = HashMap<Int, List<Resource>>()
    val spritesheets = HashMap<String, Image>()
    val singleSpriteMetadata = HashMap<Short, SpriteMetadata>()
    val compoundSpriteMetadata = HashMap<Short, List<SubSpriteMetadata>>()
    val animatedSpriteFrames = HashMap<Short, List<Short?>>()

    fun loadResourcesFrom(jarFile: JarFile) {
        val resources: List<Resource>
        getResourceFileInputStream(jarFile).use { stream ->
            resources = List(stream.readShort().toInt()) {
                Resource(
                    path = stream.readUTF(),
                    offset = stream.readInt(),
                    length = stream.readInt()
                )
            }
            val batchCount = stream.readShort().toInt()
            repeat(batchCount) {
                val type = ResourceType.fromCode(stream.readByte())
                val resourceCount = stream.readByte().toInt()
                val offset = resources[stream.readShort().toInt()].offset

                val resourcesInBatch = List(resourceCount) {
                    resources[stream.readShort().toInt()]
                }.onEach { it.type = type }

                when (type) {
                    ResourceType.IMAGE -> imagesAtOffset[offset] = resourcesInBatch
                    else -> { /* For now no process */ }
                }
            }
        }

        resources.filter { it.type == ResourceType.IMAGE }.forEach { png ->
            spritesheets[png.path] = Image(png.getInputStream(jarFile))
        }
        for ((offset, images) in imagesAtOffset) {

            getSpriteDataFileInputStream(jarFile, offset).use { stream ->
                val baseImageId = stream.readShort()
                val spriteCount = stream.readShort().toInt()
                val extraLength = stream.readShort()
                val imageMapCount = stream.readShort().toInt()
                val extraOffsets = buildMap {
                    repeat(spriteCount) {
                        put(stream.readShort(), stream.readShort())
                    }
                }
                val extraData = stream.readNBytes(extraLength.toInt())
                repeat (imageMapCount) {
                    singleSpriteMetadata[stream.readShort()] = SpriteMetadata.readFromStream(stream, images, baseImageId)
                }

                loadExtraSprites(extraData, extraOffsets, images, baseImageId)
            }
        }
    }

    fun loadExtraSprites(
        extraData: ByteArray,
        extraOffsets: Map<Short, Short>,
        spritesheets: List<Resource>,
        baseImageId: Short
    ) {
        for ((spriteID, offset) in extraOffsets) {
            DataInputStream(ByteArrayInputStream(extraData)).apply {
                skip(offset.toLong())

                val flags = readByte().toInt()
                val is16bit = (flags and 4) != 0

                when (SpriteType.fromCode(flags % 4)) {
                    SpriteType.SIMPLE -> {
                        singleSpriteMetadata[spriteID] = SpriteMetadata.readFromStream(this, spritesheets, baseImageId, is16bit)
                    }

                    SpriteType.COMPOUND -> {
                        skip(if (is16bit) 8 else 4) /* 4 Unknown values */
                        compoundSpriteMetadata[spriteID] = List(readShort().toInt()) {
                            SubSpriteMetadata.readFromStream(this, is16bit)
                        }
                    }

                    SpriteType.ANIMATED -> {
                        animatedSpriteFrames[spriteID] = List(readShort().toInt()) {
                            readShort()
                        }
                    }
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

    fun getSpriteDataFileInputStream(jarFile: JarFile, offset: Int? = null): DataInputStream {
        val entry = jarFile.getJarEntry("b")
            ?: throw IllegalArgumentException("Could not find sprite data file (/b)")
        val inputStream = DataInputStream(jarFile.getInputStream(entry))
        offset?.let { inputStream.skip(it.toLong()) }
        return inputStream
    }
}