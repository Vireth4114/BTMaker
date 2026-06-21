package objects.sprite

import javafx.collections.FXCollections
import objects.GameObject

class Sprite: GameObject() {
    override val type = 9

    val imagesX = FXCollections.observableArrayList<Int>()
    val imagesY = FXCollections.observableArrayList<Int>()
    val imageIDs = FXCollections.observableArrayList<Int>()
}