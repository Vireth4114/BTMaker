package objects.geometry

import btmaker.BitUtils
import javafx.scene.paint.Color
import objects.GameObjectReader
import java.io.DataInput
import kotlin.math.ceil

class GeometryReader: GameObjectReader<Geometry>() {
    override fun createInstance() = Geometry()

    override fun readSpecific(input: DataInput, obj: Geometry): Unit = obj.run {
        val vertexCount = input.readShort()
        val polygonCount = input.readShort()

        val alpha = input.readUnsignedByte() / 255.0
        color = Color.rgb(input.readUnsignedByte(), input.readUnsignedByte(), input.readUnsignedByte(), alpha)

        var bitSize = input.readByte().toInt()
        var base = input.readShort()

        var fullSize = ceil(bitSize * vertexCount / 8.0).toInt()
        val vertexPosBuffer = ByteArray(fullSize)
        input.readFully(vertexPosBuffer)
        verticesX.addAll(BitUtils.readStreamWithBitSize(vertexPosBuffer, bitSize).map { it + base })

        base = input.readShort()
        input.readFully(vertexPosBuffer)
        verticesY.addAll(BitUtils.readStreamWithBitSize(vertexPosBuffer, bitSize).map { it + base })

        bitSize = input.readByte().toInt()
        fullSize = ceil(bitSize * polygonCount / 8.0).toInt()
        val triangulationBuffer = ByteArray(fullSize)
        input.readFully(triangulationBuffer)
        triangulationIndices.addAll(BitUtils.readStreamWithBitSize(triangulationBuffer, bitSize))
    }
}