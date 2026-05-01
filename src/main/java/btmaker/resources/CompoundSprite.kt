package btmaker.resources

import javafx.scene.Group

class CompoundSprite(metadata: List<SubSpriteMetadata>): Group() {
    init {
        children.addAll(
            metadata.map { subSpriteMetadata ->
                ResourceManager.getSpriteById(subSpriteMetadata.imageID.toShort()).apply {
                    layoutX = subSpriteMetadata.drawX.toDouble()
                    layoutY = subSpriteMetadata.drawY.toDouble()
                }
            }
        )
    }
}