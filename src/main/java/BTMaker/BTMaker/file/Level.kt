package BTMaker.BTMaker.file

import java.io.DataInputStream
import java.util.jar.JarFile

class Level(private val index: Int) {
    private val fileName get() = "b${'f' + index}"

    fun isBonus() = index < 12

    fun getChapterName(chapterIndex: Int): String {
        return if (isBonus()) {
            "Chapter ${chapterIndex + 1}"
        } else {
            "Bonus Chapter ${chapterIndex - 11}"
        }
    }

    fun getInputStream(): DataInputStream {
        JarFile(path).use { jarFile ->
            val entry = jarFile.getJarEntry(fileName)
                ?: throw IllegalArgumentException("Could not find entry for level " + levelIndex)
            return DataInputStream(jarFile.getInputStream(entry))
        }
    }
}