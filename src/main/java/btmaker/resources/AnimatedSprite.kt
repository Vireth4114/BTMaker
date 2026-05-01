package btmaker.resources

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.scene.Group
import javafx.scene.Node
import javafx.util.Duration

class AnimatedSprite(frames: List<Node>, frameInterval: Double = 150.0): Group(frames) {
    private var index = 0
    private val timeline: Timeline

    init {
        frames.forEach { it.isVisible = false }
        if (frames.isNotEmpty()) frames[0].isVisible = true

        timeline = Timeline(
            KeyFrame(Duration.millis(frameInterval), {
                frames[index].isVisible = false
                index = (index + 1) % frames.size
                frames[index].isVisible = true
            })
        )
        timeline.cycleCount = Timeline.INDEFINITE
        timeline.play()
    }
}