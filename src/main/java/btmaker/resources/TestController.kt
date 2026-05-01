package btmaker.resources

import btmaker.resources.ResourceManager.loadResourcesFrom
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import java.net.URL
import java.util.ResourceBundle
import java.util.jar.JarFile

class TestController : Initializable {
    @FXML lateinit var pane: Pane
    var mouseX = 0.0
    var mouseY = 0.0

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        pane.background = Background(BackgroundFill(Color.LIGHTBLUE, null, null))
        val jarFile = JarFile("""C:\Users\rapha\Documents\KEmulator\bouncetales_tasdvtete.jar""")
        loadResourcesFrom(jarFile)
        var x = 0.0
        var y = 0.0
        var maxY = 0.0
        for (i in 0..1000) {
            var sprite: Node
            try {
                sprite = ResourceManager.getSpriteById((i/2).toShort()).apply {
                    layoutX = x
                    layoutY = y
                }
            } catch (_: Exception) {
                println("Failed to load sprite with ID $i")
                continue
            }
            pane.children.add(sprite)
            x += sprite.boundsInParent.width + 10
            if (x > 1500) {
                x = 0.0
                y += maxY + 10
                maxY = 0.0
            }
            maxY = maxY.coerceAtLeast(sprite.boundsInParent.height)
        }

        pane.onMouseMoved = {
            mouseX = it.x
            mouseY = it.y
        }

        pane.onMouseDragged = { event ->
            pane.children.forEach {
                it.layoutX += event.x - mouseX
                it.layoutY += event.y - mouseY
            }
            mouseX = event.x
            mouseY = event.y
        }
    }
}