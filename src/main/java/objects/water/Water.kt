package objects.water


import javafx.beans.property.SimpleObjectProperty
import javafx.scene.paint.Color
import objects.zone.Zone
import properties.SimpleByteProperty

class Water: Zone() {
    val gravityTopProperty = SimpleByteProperty()
    var gravityTop: Byte
        get() = gravityTopProperty.get().toByte()
        set(value) = gravityTopProperty.set(value)

    val gravityRightProperty = SimpleByteProperty()
    var gravityRight: Byte
        get() = gravityRightProperty.get().toByte()
        set(value) = gravityRightProperty.set(value)

    val gravityBottomProperty = SimpleByteProperty()
    var gravityBottom: Byte
        get() = gravityBottomProperty.get().toByte()
        set(value) = gravityBottomProperty.set(value)

    val gravityLeftProperty = SimpleByteProperty()
    var gravityLeft: Byte
        get() = gravityLeftProperty.get().toByte()
        set(value) = gravityLeftProperty.set(value)

    val colorProperty = SimpleObjectProperty<Color>()
    var color: Color?
        get() = colorProperty.get()
        set(value) = colorProperty.set(value)
}