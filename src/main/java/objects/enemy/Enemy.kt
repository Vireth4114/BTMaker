package objects.enemy

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import objects.GameObject
import properties.SimpleShortProperty

class Enemy: GameObject() {
    override val type = 15

    val enemyTypeProperty: ObjectProperty<EnemyType?> = SimpleObjectProperty()
    var enemyType: EnemyType?
        get() = enemyTypeProperty.get()
        set(value) = enemyTypeProperty.set(value)

    val startXProperty = SimpleShortProperty()
    var startX: Short
        get() = startXProperty.get().toShort()
        set(value) = startXProperty.set(value)

    val startYProperty = SimpleShortProperty()
    var startY: Short
        get() = startYProperty.get().toShort()
        set(value) = startYProperty.set(value)

    val endXProperty = SimpleShortProperty()
    var endX: Short
        get() = endXProperty.get().toShort()
        set(value) = endXProperty.set(value)

    val endYProperty = SimpleShortProperty()
    var endY: Short
        get() = endYProperty.get().toShort()
        set(value) = endYProperty.set(value)
}