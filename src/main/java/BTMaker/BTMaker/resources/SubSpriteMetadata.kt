package BTMaker.BTMaker.resources

import java.io.DataInputStream

data class SubSpriteMetadata(
    val drawX: Int,
    val drawY: Int,
    val imageID: Int
) {
    companion object {
        fun readFromStream(
            data: DataInputStream,
            is16Bit: Boolean
        ): SubSpriteMetadata {
            return internalReadFromStream(data) {
                if (is16Bit) it.readShort().toInt() else it.readByte().toInt()
            }
        }

        private fun internalReadFromStream(
            data: DataInputStream,
            reader: (DataInputStream) -> Int
        ): SubSpriteMetadata {
            return SubSpriteMetadata(
                drawX = reader(data),
                drawY = reader(data),
                imageID = reader(data)
            )
        }
    }
}