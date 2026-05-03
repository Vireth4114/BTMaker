package properties

import javafx.beans.property.SimpleIntegerProperty

class SimpleShortProperty(initialValue: Int = 0) : SimpleIntegerProperty(initialValue) {
    override fun set(newValue: Int) = super.set(newValue.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()))
    fun set(newValue: Short) = super.set(newValue.toInt())
}