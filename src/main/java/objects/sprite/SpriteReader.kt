package objects.sprite

import btmaker.BitUtils
import objects.GameObjectReader
import java.io.DataInput
import kotlin.math.ceil

class SpriteReader: GameObjectReader<Sprite>() {
    override fun createInstance() = Sprite()

    override fun readSpecific(input: DataInput, obj: Sprite): Unit = obj.run {
        val count = input.readUnsignedByte()
        if (count == 0) return

        val bitSize = input.readByte().toInt()
        var fullSize = ceil(bitSize * count / 8.0).toInt()
        val baseX = input.readShort()
        val baseY = input.readShort()
        if (bitSize > 0) {
            val posBuffer = ByteArray(fullSize)
            input.readFully(posBuffer)
            imagesX.addAll(BitUtils.readStreamWithBitSize(posBuffer, bitSize).map { it + baseX })
            input.readFully(posBuffer)
            imagesY.addAll(BitUtils.readStreamWithBitSize(posBuffer, bitSize).map { it + baseY })
        } else {
            repeat (count) {
                imagesX.add(baseX.toInt())
                imagesY.add(baseY.toInt())
            }
        }

        fullSize = 2 * count
        val idBuffer = ByteArray(fullSize)
        input.readFully(idBuffer)
        imageIDs.addAll(BitUtils.readStreamWithBitSize(idBuffer, 16))
    }
}