package objects.cannon

import objects.GameObject
import properties.SimpleByteProperty
import properties.SimpleShortProperty

class Cannon: GameObject() {
    override val type = 11

    val playerIdProperty = SimpleShortProperty()
    var playerId: Short
        get() = playerIdProperty.get().toShort()
        set(value) = playerIdProperty.set(value)

    val powerProperty = SimpleByteProperty()
    var power: Byte
        get() = powerProperty.get().toByte()
        set(value) = powerProperty.set(value)
}