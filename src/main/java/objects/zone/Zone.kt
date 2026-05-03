package objects.zone

import objects.GameObject
import properties.SimpleShortProperty

abstract class Zone: GameObject() {
    val minXProperty = SimpleShortProperty()
    var minX: Short
        get() = minXProperty.get().toShort()
        set(value) = minXProperty.set(value)

    val maxXProperty = SimpleShortProperty()
    var maxX: Short
        get() = maxXProperty.get().toShort()
        set(value) = maxXProperty.set(value)

    val minYProperty = SimpleShortProperty()
    var minY: Short
        get() = minYProperty.get().toShort()
        set(value) = minYProperty.set(value)

    val maxYProperty = SimpleShortProperty()
    var maxY: Short
        get() = maxYProperty.get().toShort()
        set(value) = maxYProperty.set(value)
}