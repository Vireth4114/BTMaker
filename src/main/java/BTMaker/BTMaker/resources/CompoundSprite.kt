package BTMaker.BTMaker.resources

import javafx.scene.Group

class CompoundSprite(metadata: List<SubSpriteMetadata>): Group() {
    init {
        children.addAll(
            metadata.map { subSpriteMetadata ->
                ResourceManager.sprites[subSpriteMetadata.imageID.toShort()].apply {
                    layoutX = subSpriteMetadata.drawX.toDouble()
                    layoutY = subSpriteMetadata.drawY.toDouble()
                    println("Loaded subsprite with image ID ${subSpriteMetadata.imageID} at (${subSpriteMetadata.drawX}, ${subSpriteMetadata.drawY})")
                }
            }
        )
    }
}