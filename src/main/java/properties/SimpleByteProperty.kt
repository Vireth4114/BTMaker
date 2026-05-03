package properties

import javafx.beans.property.SimpleIntegerProperty

class SimpleByteProperty(initialValue: Int = 0) : SimpleIntegerProperty(initialValue) {
    override fun set(newValue: Int) = super.set(newValue.coerceIn(Byte.MIN_VALUE.toInt(), Byte.MAX_VALUE.toInt()))
    fun set(newValue: Byte) = super.set(newValue.toInt())
}