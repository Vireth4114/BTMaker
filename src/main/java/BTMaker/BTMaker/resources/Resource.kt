package BTMaker.BTMaker.resources

import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.jar.JarFile

class Resource(var path: String, var offset: Int = 0, var length: Int? = null) {
    var type: ResourceType? = null

    fun getInputStream(jarFile: JarFile): DataInputStream {
        val entry = jarFile.getJarEntry(path)
            ?: throw IllegalArgumentException("Could not find resource file (/a)")
        return DataInputStream(jarFile.getInputStream(entry))
    }
}