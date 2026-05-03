package objects.event

import javafx.collections.FXCollections
import objects.zone.ZoneReader
import java.io.DataInput

class EventReader: ZoneReader<Event>() {
    override fun createInstance() = Event()

    override fun readSpecificZone(input: DataInput, obj: Event) = obj.run {
        state = input.readByte()
        triggerLeave = input.readByte()
        repeatable = input.readByte()
        triggerId = input.readShort()
        val eventCount = input.readByte().toInt()
        repeat(eventCount) {
            val dataCount = input.readByte().toInt() - 1
            val key = input.readByte()
            val bytes = List(dataCount) {
                input.readByte()
            }
            eventData[key] = FXCollections.observableArrayList(bytes)
        }
    }
}