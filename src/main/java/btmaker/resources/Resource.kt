package btmaker.resources

import java.io.DataInputStream
import java.util.jar.JarFile

class Resource(var path: String, var offset: Int, var length: Int) {
    var type: ResourceType? = null

    fun getInputStream(jarFile: JarFile): DataInputStream {
        val entry = jarFile.getJarEntry(path)
            ?: throw IllegalArgumentException("Could not find resource file (/a)")
        return DataInputStream(jarFile.getInputStream(entry))
    }
}