package objects.sprite

import btmaker.BitUtils
import objects.GameObjectWriter
import java.io.DataOutput
import kotlin.math.max

class SpriteWriter: GameObjectWriter<Sprite>() {
    override fun writeSpecific(output: DataOutput, obj: Sprite): Unit = output.run {
        writeByte(obj.imageIDs.size)
        if (obj.imageIDs.isEmpty()) return

        val (bitSizeX, baseX) = BitUtils.getOptimalBitSizeAndBase(obj.imagesX)
        val (bitSizeY, baseY) = BitUtils.getOptimalBitSizeAndBase(obj.imagesY)
        val bitSize = max(bitSizeX, bitSizeY)

        writeByte(bitSize)
        writeShort(baseX)
        writeShort(baseY)

        if (bitSize > 0) {
            write(BitUtils.bitSizeToByteStream(obj.imagesX, bitSize, baseX))
            write(BitUtils.bitSizeToByteStream(obj.imagesY, bitSize, baseY))
        }

        write(BitUtils.bitSizeToByteStream(obj.imageIDs, 16, 0))
    }
}