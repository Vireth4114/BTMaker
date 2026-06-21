package objects.geometry

import btmaker.BitUtils
import objects.GameObjectWriter
import java.io.DataOutput
import kotlin.math.max

class GeometryWriter: GameObjectWriter<Geometry>() {
    override fun writeSpecific(output: DataOutput, obj: Geometry) = output.run {
        writeShort(obj.verticesX.size)
        writeShort(obj.triangulationIndices.size)

        writeByte(((obj.color?.opacity ?: 1.0) * 255.0).toInt())
        writeByte(((obj.color?.red ?: 1.0) * 255.0).toInt())
        writeByte(((obj.color?.green ?: 1.0) * 255.0).toInt())
        writeByte(((obj.color?.blue ?: 1.0) * 255.0).toInt())

        val (bitSizeX, baseX) = BitUtils.getOptimalBitSizeAndBase(obj.verticesX)
        val (bitSizeY, baseY) = BitUtils.getOptimalBitSizeAndBase(obj.verticesY)
        val bitSize = max(bitSizeX, bitSizeY)
        val (bitSizeTriangulation, baseTriangulation) = BitUtils.getOptimalBitSizeAndBase(obj.triangulationIndices)

        writeByte(bitSize)
        writeShort(baseX)
        write(BitUtils.bitSizeToByteStream(obj.verticesX, bitSize, baseX))

        writeShort(baseY)
        write(BitUtils.bitSizeToByteStream(obj.verticesY, bitSize, baseY))

        writeByte(bitSizeTriangulation)
        writeShort(baseTriangulation)
        write(BitUtils.bitSizeToByteStream(obj.triangulationIndices, bitSizeTriangulation, baseTriangulation))
    }
}