package objects.event

import objects.zone.ZoneWriter
import java.io.DataOutput

class EventWriter: ZoneWriter<Event>() {
    override fun writeSpecificZone(output: DataOutput, obj: Event) = output.run {
        writeByte(obj.state.toInt())
        writeByte(obj.triggerLeave.toInt())
        writeByte(obj.repeatable.toInt())
        writeShort(obj.triggerId.toInt())
        writeByte(obj.eventData.size)
        obj.eventData.forEach {(key, data) ->
            writeByte(data.size + 1)
            writeByte(key.toInt())
            write(data.toByteArray())
        }
    }
}