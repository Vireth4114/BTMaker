package btmaker

import java.io.DataOutput
import java.io.DataOutputStream
import java.io.OutputStream

/**
 * A DataOutputStream that counts the number of bytes written.
 */
class CountingDataOutputStream(private val stream: DataOutputStream) : DataOutput, OutputStream() {
    var bytesWritten = 0

    constructor(buffer: OutputStream) : this(DataOutputStream(buffer))

    override fun write(b: Int) = stream.write(b).also { bytesWritten += 1 }
    override fun write(b: ByteArray) = stream.write(b).also { bytesWritten += b.size }
    override fun write(b: ByteArray, off: Int, len: Int) = stream.write(b, off, len).also { bytesWritten += len }
    override fun writeBoolean(v: Boolean) = stream.writeBoolean(v).also { bytesWritten += 1 }
    override fun writeByte(v: Int) = stream.writeByte(v).also { bytesWritten += 1 }
    override fun writeShort(v: Int) = stream.writeShort(v).also { bytesWritten += 2 }
    override fun writeChar(v: Int) = stream.writeChar(v).also { bytesWritten += 2 }
    override fun writeInt(v: Int) = stream.writeInt(v).also { bytesWritten += 4 }
    override fun writeLong(v: Long) = stream.writeLong(v).also { bytesWritten += 8 }
    override fun writeFloat(v: Float) = stream.writeFloat(v).also { bytesWritten += 4 }
    override fun writeDouble(v: Double) = stream.writeDouble(v).also { bytesWritten += 8 }
    override fun writeBytes(s: String) = stream.writeBytes(s).also { bytesWritten += s.length }
    override fun writeChars(s: String) = stream.writeChars(s).also { bytesWritten += s.length * 2 }
    override fun writeUTF(s: String) = stream.writeUTF(s)
}