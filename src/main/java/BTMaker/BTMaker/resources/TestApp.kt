package BTMaker.BTMaker.resources

import BTMaker.BTMaker.resources.ResourceManager.loadResourcesFrom
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import java.util.jar.JarFile

class TestApp: Application() {

    override fun start(stage: Stage) {
        stage.icons.add(Image("/icon.png"))
        stage.title = "BTMaker 0.2.0"
        val fxmlLoader = FXMLLoader(javaClass.getResource("/test.fxml"))
        fxmlLoader.setController(TestController())
        val scene = Scene(fxmlLoader.load(), 1200.0, 675.0)
        stage.scene = scene
        stage.isMaximized = true
        stage.show()
    }

     companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(TestApp::class.java)
        }
     }
}