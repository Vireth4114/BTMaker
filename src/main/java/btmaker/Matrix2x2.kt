package btmaker

import kotlin.math.cos
import kotlin.math.sin

data class Matrix2x2(
    val a: Double,
    val b: Double,
    val c: Double,
    val d: Double
) {
    companion object {
        fun fromScaleAndRotation(xScale: Double, yScale: Double, rotation: Double) = Matrix2x2(
            a = xScale * cos(rotation),
            b = xScale * sin(rotation),
            c = -yScale * sin(rotation),
            d = yScale * cos(rotation)
        )
    }
}
