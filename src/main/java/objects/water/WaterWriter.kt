package objects.water

import objects.zone.ZoneWriter
import java.io.DataOutput

class WaterWriter: ZoneWriter<Water>() {
    override fun write(output: DataOutput, objectToWrite: Water) {

    }
}