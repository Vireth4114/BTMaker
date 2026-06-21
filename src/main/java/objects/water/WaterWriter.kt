package objects.water

import objects.zone.ZoneWriter
import java.io.DataOutput

class WaterWriter: ZoneWriter<Water>() {
    override fun writeSpecificZone(output: DataOutput, obj: Water) = output.run {
        writeByte(obj.gravityTop.toInt())
        writeByte(obj.gravityRight.toInt())
        writeByte(obj.gravityBottom.toInt())
        writeByte(obj.gravityLeft.toInt())

        writeByte(((obj.color?.opacity ?: 1.0) * 255.0).toInt())
        writeByte(((obj.color?.red ?: 1.0) * 255.0).toInt())
        writeByte(((obj.color?.green ?: 1.0) * 255.0).toInt())
        writeByte(((obj.color?.blue ?: 1.0) * 255.0).toInt())
    }
}