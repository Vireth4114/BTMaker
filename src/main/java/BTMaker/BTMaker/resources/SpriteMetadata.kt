package BTMaker.BTMaker.resources

import java.io.DataInputStream

data class SpriteMetadata(
    val width: Int,
    val height: Int,
    val originX: Int,
    val originY: Int,
    val atlasX: Int,
    val atlasY: Int,
    val image: String
) {
    companion object {
        fun readFromStream(
            data: DataInputStream,
            metadataLoadingContext: MetadataLoadingContext
        ): SpriteMetadata {
            val is16Bit = metadataLoadingContext.is16Bit
            return internalReadFromStream(
                data,
                metadataLoadingContext.spritesheets,
                metadataLoadingContext.baseSpritesheetId,
                if (is16Bit) { it -> it.readShort().toInt() } else { it -> it.readByte().toInt() },
                if (is16Bit) { it -> it.readUnsignedShort() } else { it -> it.readUnsignedByte() }
            )
        }

        private fun internalReadFromStream(
            data: DataInputStream,
            spritesheetsInBatch: List<Resource>,
            baseSpritesheetId: Short,
            reader: (DataInputStream) -> Int,
            readerUnsigned: (DataInputStream) -> Int
        ): SpriteMetadata {
            return SpriteMetadata(
                width = readerUnsigned(data),
                height = readerUnsigned(data),
                originX = reader(data),
                originY = reader(data),
                atlasX = readerUnsigned(data),
                atlasY = readerUnsigned(data),
                image = spritesheetsInBatch[readerUnsigned(data) - baseSpritesheetId].path
            )
        }
    }
}