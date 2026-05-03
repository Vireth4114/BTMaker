package objects.event

import objects.zone.ZoneWriter
import java.io.DataOutput

class EventWriter: ZoneWriter<Event>() {
    override fun write(output: DataOutput, objectToWrite: Event) {

    }
}