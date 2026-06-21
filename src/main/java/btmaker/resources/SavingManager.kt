package btmaker.resources

import java.io.File
import java.util.jar.JarOutputStream

class SavingManager(val file: File) {

    fun saveChapters() {
        JarOutputStream(file.outputStream()).use {
            ChapterManager.saveChapters(it)
        }
    }

//    fun saveBatchMapping(jarFile: JarFile) = getResourceFileInputStream(jarFile).apply {
//        val resources = List(readShort().toInt()) {
//            Resource(
//                path = readUTF(),
//                offset = readInt(),
//                length = readInt()
//            )
//        }
//        val batchCount = readShort().toInt()
//        repeat(batchCount) {
//            val type = ResourceType.fromCode(readByte())
//            val resourceCount = readByte().toInt()
//            val batchData = resources[readShort().toInt()].apply {
//                this.type = type
//            }
//
//            val resourcesInBatch = List(resourceCount) {
//                resources[readShort().toInt()]
//            }.onEach { it.type = type }
//
//            if (type == ResourceType.IMAGE) {
//                batchAtOffset[batchData.offset] = resourcesInBatch
//            }
//        }
//
//        resources.filter { it.type == ResourceType.IMAGE }.forEach { png ->
//            spritesheets[png.path] = Image(png.getInputStream(jarFile))
//        }
//    }
}