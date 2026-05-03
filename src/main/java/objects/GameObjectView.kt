package objects

import javafx.scene.Node

abstract class GameObjectView<T: GameObject>(val model: T) {
    lateinit var node: Node

    init {
        model.xPosProperty.bind(node.layoutXProperty())
        model.yPosProperty.bind(node.layoutYProperty())
        model.rotationProperty.bind(node.rotateProperty())
        model.xScaleProperty.bind(node.scaleXProperty())
        model.yScaleProperty.bind(node.scaleYProperty())
        model.zIndexProperty.bind(node.viewOrderProperty())
        model.noDrawProperty.bind(node.visibleProperty())
    }
}