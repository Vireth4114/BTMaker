package BTMaker.BTMaker.resources

import javafx.geometry.Rectangle2D
import javafx.scene.image.ImageView

class SimpleSprite(metadata: SpriteMetadata): ImageView(ResourceManager.spritesheets[metadata.image]) {
    init {
        viewport = Rectangle2D(
            metadata.atlasX.toDouble(),
            metadata.atlasY.toDouble(),
            metadata.width.toDouble(),
            metadata.height.toDouble()
        )
        fitWidth = metadata.width.toDouble()
        fitHeight = metadata.height.toDouble()

        translateX = -metadata.originX.toDouble()
        translateY = -metadata.originY.toDouble()
    }
}