package objects

import btmaker.CountingDataOutputStream
import btmaker.Matrix2x2
import java.io.ByteArrayOutputStream
import java.io.DataOutput
import java.io.DataOutputStream
import java.io.OutputStream

abstract class GameObjectWriter<T: GameObject> {
    fun write(output: DataOutputStream, obj: T, previousId: Int = -1): Int {
        val buffer = ByteArrayOutputStream()
        val bufferOutput = CountingDataOutputStream(buffer)

        writeCommon(bufferOutput, obj, previousId)
        writeSpecific(bufferOutput, obj)

        output.writeShort(bufferOutput.bytesWritten)
        output.write(buffer.toByteArray())

        return bufferOutput.bytesWritten + 2
    }

    private fun writeCommon(output: DataOutput, obj: GameObject, previousId: Int = -1) = output.run {
        writeShort(obj.parentID.toInt())
        writeShort(previousId)

        var transformFlags = 1

        if (obj.rotation != 0.0) {
            transformFlags = transformFlags or 2
        }

        if (obj.xScale != 1.0 || obj.yScale != 1.0) {
            transformFlags = transformFlags or 4
        }

        writeByte(transformFlags)

        if ((transformFlags and 7) == 7) {
            val matrix = Matrix2x2.fromScaleAndRotation(obj.xScale, obj.yScale, obj.rotation)
            writeInt((matrix.a * 65536.0).toInt())
            writeInt((matrix.b * 65536.0).toInt())
            writeInt((obj.xPos * 65536.0).toInt())
            writeInt((matrix.c * 65536.0).toInt())
            writeInt((matrix.d * 65536.0).toInt())
            writeInt((obj.yPos * 65536.0).toInt())
        } else {
            writeShort(obj.xPos.toInt())
            writeShort(obj.yPos.toInt())
            if ((transformFlags and 2) > 0) {
                writeInt((obj.rotation * 65536.0).toInt())
            }
            if ((transformFlags and 4) > 0) {
                writeInt((obj.xScale * 65536.0).toInt())
                writeInt((obj.yScale * 65536.0).toInt())
            }
        }

        var flags = obj.zIndex and 0x1f
        if (obj.noDraw) {
            flags = flags or 0x80
        }
        if (obj.noCollision) {
            flags = flags or 0x20
        }
        writeInt(flags)
    }

    protected abstract fun writeSpecific(output: DataOutput, obj: T)
}