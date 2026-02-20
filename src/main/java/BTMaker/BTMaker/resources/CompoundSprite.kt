package BTMaker.BTMaker.resources

import BTMaker.BTMaker.Controller
import javafx.scene.Group

class CompoundSprite(metadata: List<SubSpriteMetadata>): Group {
    init {
        val scaleBinding = Controller.instance.zoomLevel.multiply(ResourceManager.SPRITE_SCALE)

        for (subSpriteMetadata in metadata) {
            val subSprite = ResourceManager.getSpriteById(subSpriteMetadata.imageID.toShort())
            subSprite.layoutXProperty().bind(scaleBinding.multiply(subSpriteMetadata.drawX))
            subSprite.layoutYProperty().bind(scaleBinding.multiply(subSpriteMetadata.drawY))
            children.add(subSprite)
        }
    }
}