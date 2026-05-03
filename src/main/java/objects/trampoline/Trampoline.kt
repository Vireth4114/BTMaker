package objects.trampoline

import objects.GameObject
import properties.SimpleByteProperty
import properties.SimpleShortProperty

class Trampoline: GameObject() {
    val imageIdProperty = SimpleShortProperty()
    var imageId: Short
        get() = imageIdProperty.get().toShort()
        set(value) = imageIdProperty.set(value)

    val pushForceProperty = SimpleByteProperty()
    var pushForce: Byte
        get() = pushForceProperty.get().toByte()
        set(value) = pushForceProperty.set(value)
}