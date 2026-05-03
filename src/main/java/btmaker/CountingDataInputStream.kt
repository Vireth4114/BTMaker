package btmaker

import java.io.DataInput
import java.io.DataInputStream

/**
 * A DataInputStream that counts the number of bytes read.
 * readUTF and readLine are not counted because they read a variable number of bytes.
 */
class CountingDataInputStream(private val stream: DataInputStream) : DataInput {
    var bytesRead = 0

    override fun readFully(b: ByteArray) {
        stream.readFully(b)
        bytesRead += b.size
    }

    override fun readFully(b: ByteArray, off: Int, len: Int) {
        stream.readFully(b, off, len)
        bytesRead += len
    }

    override fun skipBytes(n: Int) = stream.skipBytes(n).also { bytesRead += it }
    override fun readBoolean() = stream.readBoolean().also { bytesRead += 1 }
    override fun readByte() = stream.readByte().also { bytesRead += 1 }
    override fun readUnsignedByte() = stream.readUnsignedByte().also { bytesRead += 1 }
    override fun readShort() = stream.readShort().also { bytesRead += 2 }
    override fun readUnsignedShort() = stream.readUnsignedShort().also { bytesRead += 2 }
    override fun readChar() = stream.readChar().also { bytesRead += 2 }
    override fun readInt() = stream.readInt().also { bytesRead += 4 }
    override fun readLong() = stream.readLong().also { bytesRead += 8 }
    override fun readFloat() = stream.readFloat().also { bytesRead += 4 }
    override fun readDouble() = stream.readDouble().also { bytesRead += 8 }

    override fun readUTF(): String = stream.readUTF()
    override fun readLine(): String? = stream.readLine()

    inline fun <T> chunk(length: Short, block: CountingDataInputStream.() -> T): T {
        val start = bytesRead
        val result = block()
        val consumed = bytesRead - start

        val remaining = length - consumed
        if (remaining > 0) {
            skipBytes(remaining)
        }

        return result
    }
}