package btmaker

object BitUtils {
    fun readStreamWithBitSize(bytes: ByteArray, bitSize: Int): List<Int> {
        if (bytes.isEmpty()) return listOf()

        val totalBits = bytes.size * 8
        val usableBits = totalBits - (totalBits % bitSize)
        val size = usableBits / bitSize

        val result = ArrayList<Int>(size)

        var buffer = 0L
        var bitsInBuffer = 0
        var outIndex = size - 1

        for (i in bytes.indices.reversed()) {
            buffer = buffer or ((bytes[i].toLong() and 0xFF) shl bitsInBuffer)
            bitsInBuffer += 8

            while (bitsInBuffer >= bitSize) {
                var value = (buffer and ((1L shl bitSize) - 1)).toInt()

                if ((value and (1 shl (bitSize - 1))) != 0) {
                    value -= (1 shl bitSize)
                }

                result[outIndex--] = value

                buffer = buffer shr bitSize
                bitsInBuffer -= bitSize
            }
        }

        return result
    }

    fun bitSizeToByteStream(values: List<Int>, n: Int, base: Int): ByteArray {
        val buffer = mutableListOf<Int>()

        for (i in values.indices.reversed()) {
            var v = values[i] - base

            if (v < 0) {
                v += (1 shl n)
            }

            for (b in 0 until n) {
                buffer.add((v shr b) and 1)
            }
        }

        val byteCount = buffer.size / 8
        val result = ByteArray(byteCount)

        for (i in 0 until byteCount) {
            var byte = 0
            for (b in 0 until 8) {
                byte = byte or (buffer[i * 8 + b] shl b)
            }
            result[byteCount - 1 - i] = byte.toByte()
        }

        return result
    }
}