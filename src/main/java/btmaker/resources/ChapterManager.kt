package btmaker.resources

import objects.Chapter
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream


object ChapterManager {
    val chapters = mutableListOf<Chapter>()

    fun getChapterFileName(num: Int) = "b${'f' + num}"

    fun loadChapters(jarFile: JarFile) {
        for (i in 0..15) {
            println("Loading ${getChapterFileName(i)}")
            val entry = jarFile.getJarEntry(getChapterFileName(i)) ?: continue
            val chapter = Chapter().also { chapters.add(it) }
            jarFile.getInputStream(entry).use {
                chapter.readObjects(it)
            }
        }
    }

    fun saveChapters(stream: JarOutputStream) = stream.apply {
        for (i in 0..15) {
            println("Saving ${getChapterFileName(i)}")
            val chapter = chapters.getOrNull(i) ?: continue
            putNextEntry(JarEntry(getChapterFileName(i)))
            chapter.writeObjects(this)
            closeEntry()
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val inputJar = File("""C:\Users\rapha\Documents\KEmulator\bouncetales_tasdvtete.jar""")
        val outputJar = File("""C:\Users\rapha\Documents\KEmulator\bouncetales_tasdvtete2.jar""")

        val chapterNames = (0..15).map { getChapterFileName(it) }.toSet()

        JarFile(inputJar).use { jar ->
            loadChapters(jar)

            JarOutputStream(FileOutputStream(outputJar)).use { jos ->

                val entries = jar.entries()

                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()

                    if (entry.name in chapterNames) continue

                    jar.getInputStream(entry).use { input ->
                        val newEntry = JarEntry(entry.name)
                        jos.putNextEntry(newEntry)
                        input.copyTo(jos)
                        jos.closeEntry()
                    }
                }

                saveChapters(jos)
            }
        }
    }
}