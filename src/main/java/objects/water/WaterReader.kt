package objects.water

import javafx.scene.paint.Color
import objects.zone.ZoneReader
import java.io.DataInput

class WaterReader: ZoneReader<Water>() {
    override fun createInstance() = Water()

    override fun readSpecificZone(input: DataInput, obj: Water) = obj.run {
        gravityTop = input.readByte()
        gravityRight = input.readByte()
        gravityBottom = input.readByte()
        gravityLeft = input.readByte()
        val alpha = input.readUnsignedByte() / 255.0
        color = Color.rgb(input.readUnsignedByte(), input.readUnsignedByte(), input.readUnsignedByte(), alpha)
    }
}