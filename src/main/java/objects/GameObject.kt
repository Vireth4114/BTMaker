package objects

import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import properties.SimpleShortProperty
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot

abstract class GameObject {
    val idProperty = SimpleShortProperty()
    var id: Short
        get() = idProperty.get().toShort()
        set(value) = idProperty.set(value)

    val parentIDProperty = SimpleShortProperty(-1)
    var parentID: Short
        get() = parentIDProperty.get().toShort()
        set(value) = parentIDProperty.set(value)

    val previousIDProperty = SimpleShortProperty(-1)
    var previousID: Short
        get() = previousIDProperty.get().toShort()
        set(value) = previousIDProperty.set(value)

    val xPosProperty = SimpleShortProperty(0)
    var xPos: Short
        get() = xPosProperty.get().toShort()
        set(value) = xPosProperty.set(value)

    val yPosProperty = SimpleShortProperty(0)
    var yPos: Short
        get() = yPosProperty.get().toShort()
        set(value) = yPosProperty.set(value)

    val rotationProperty: DoubleProperty = SimpleDoubleProperty(0.0)
    var rotation: Double
        get() = rotationProperty.get()
        set(value) = rotationProperty.set(value)

    val xScaleProperty: DoubleProperty = SimpleDoubleProperty(1.0)
    var xScale: Double
        get() = xScaleProperty.get()
        set(value) = xScaleProperty.set(value)

    val yScaleProperty: DoubleProperty = SimpleDoubleProperty(1.0)
    var yScale: Double
        get() = yScaleProperty.get()
        set(value) = yScaleProperty.set(value)

    val zIndexProperty = SimpleIntegerProperty(0)
    var zIndex: Int
        get() = zIndexProperty.get()
        set(value) = zIndexProperty.set(value)

    val noDrawProperty = SimpleBooleanProperty(false)
    var noDraw: Boolean
        get() = noDrawProperty.get()
        set(value) = noDrawProperty.set(value)

    val noCollisionProperty = SimpleBooleanProperty(false)
    var noCollision: Boolean
        get() = noCollisionProperty.get()
        set(value) = noCollisionProperty.set(value)

    /**
     * Sets the scale and rotation properties based on the given transformation matrix components.
     * Should only be used when both scale and rotation are present (i.e., when the transform flags is equal to 7).
     *
     * Rotation is between -PI and PI
     * X Scale may be negative
     * Y Scale is always positive
     */
    fun setScaleAndRotationFromMatrix(a: Double, b: Double, c: Double, d: Double) {
        var rotation = atan2(c, a)
        var xScale = hypot(a, c)
        val yScale = hypot(b, d)

        val det = a * d - b * c
        if (det < 0) {
            xScale = -xScale
            rotation += PI
            if (rotation > PI) {
                rotation -= 2 * PI
            }
        }

        this.rotation = rotation
        this.yScale = xScale
        this.xScale = yScale
    }
}