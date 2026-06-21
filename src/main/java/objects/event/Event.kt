package objects.event

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
import objects.zone.Zone
import properties.SimpleByteProperty
import properties.SimpleShortProperty

class Event: Zone() {
    override val type = 6

    val stateProperty = SimpleByteProperty()
    var state: Byte
        get() = stateProperty.get().toByte()
        set(value) = stateProperty.set(value)

    val triggerLeaveProperty = SimpleByteProperty()
    var triggerLeave: Byte
        get() = triggerLeaveProperty.get().toByte()
        set(value) = triggerLeaveProperty.set(value)
    
    val repeatableProperty = SimpleByteProperty()
    var repeatable: Byte
        get() = repeatableProperty.get().toByte()
        set(value) = repeatableProperty.set(value)
    
    val triggerIdProperty = SimpleShortProperty()
    var triggerId: Short
        get() = triggerIdProperty.get().toShort()
        set(value) = triggerIdProperty.set(value)
    
    val eventIdProperty = SimpleByteProperty()
    var eventId: Byte
        get() = eventIdProperty.get().toByte()
        set(value) = eventIdProperty.set(value)
    
    val eventData: ObservableMap<Byte, ObservableList<Byte>> = FXCollections.observableHashMap()
}