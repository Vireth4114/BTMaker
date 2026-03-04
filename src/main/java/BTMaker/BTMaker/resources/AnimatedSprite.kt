package BTMaker.BTMaker.resources

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.scene.Group
import javafx.scene.Node
import javafx.util.Duration

class AnimatedSprite(frames: List<Node>): Group(frames) {
    init {
        var index = 0

        frames.forEach { it.isVisible = false }
        if (frames.isNotEmpty()) frames[0].isVisible = true

        val timeline = Timeline(
            KeyFrame(Duration.millis(150.0), {
                frames[index].isVisible = false
                index = (index + 1) % frames.size
                frames[index].isVisible = true
            })
        )
        timeline.cycleCount = Timeline.INDEFINITE
        timeline.play()
    }
}