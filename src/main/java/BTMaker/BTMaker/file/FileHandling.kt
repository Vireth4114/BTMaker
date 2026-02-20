package BTMaker.BTMaker.file

import javafx.stage.FileChooser
import model.GameObject
import model.GeometryObject
import model.Level
import java.io.File
import java.net.URI
import java.nio.file.FileSystems
import java.util.Map

class FileHandling {

    /**
     * Select using filechooser
     * loads every level
     * loads cannon objects
     * loads resources (textures, sounds, etc.)
     * loads png files for textures
     * cut png files into textures based on the batch file
     */
    fun onOpen(path: String?) {
        if (path == null) {
            val fc = FileChooser()
            fc.getExtensionFilters().addAll(
                FileChooser.ExtensionFilter("Java Game", "*.jar", "*.jad"),
                FileChooser.ExtensionFilter("All Files", "*.*")
            )
            gameFile = fc.showOpenDialog(null)
        } else {
            gameFile = File(path)
        }
        if (gameFile == null) return

        val uri = URI.create("jar:" + gameFile.toURI())
        FileSystems.newFileSystem(uri, Map.of<String?, Any?>()).use { fs ->
            for (levelIndex in 0..14) {
                val levelPath = fs.getPath(getLevelFileName(levelIndex))
                levels.set(levelIndex, Level(levelPath))
            }
            val cannon = Level(fs.getPath("/bu"))
            for (`object` in cannon.objects.subList(1, 4)) {
                cannonShapes.add(`object` as GeometryObject?)
            }
            cannonShapes.sort(Comparator { o1: GameObject?, o2: GameObject? -> o2!!.zcoord - o1!!.zcoord })
        }
    }
}