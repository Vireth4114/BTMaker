package BTMaker.BTMaker.resources

import BTMaker.BTMaker.Controller
import javafx.geometry.Rectangle2D
import javafx.scene.image.ImageView

class SimpleSprite(metadata: SpriteMetadata): ImageView(ResourceManager.spritesheets[metadata.image]) {
    init {
        val scaleBinding = Controller.instance.zoomLevel.multiply(ResourceManager.SPRITE_SCALE)

        viewport = Rectangle2D(
            metadata.atlasX.toDouble(),
            metadata.atlasY.toDouble(),
            metadata.width.toDouble(),
            metadata.height.toDouble()
        )
        fitWidth = metadata.width.toDouble()
        fitHeight = metadata.height.toDouble()

        scaleXProperty().bind(scaleBinding)
        scaleYProperty().bind(scaleBinding)

        translateXProperty().bind(scaleBinding.multiply(-metadata.originX))
        translateYProperty().bind(scaleBinding.multiply(-metadata.originY))
    }
}