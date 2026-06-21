package objects.geometry

import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.scene.paint.Color
import objects.GameObject

class Geometry: GameObject() {
    override val type = 4

    val colorProperty = SimpleObjectProperty<Color>()
    var color: Color?
        get() = colorProperty.get()
        set(value) = colorProperty.set(value)

    val verticesX = FXCollections.observableArrayList<Int>()
    val verticesY = FXCollections.observableArrayList<Int>()

    val triangulationIndices = FXCollections.observableArrayList<Int>()
}