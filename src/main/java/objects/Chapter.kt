package objects

import objects.event.Event
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream

class Chapter {
    val objects: ArrayList<GameObject> = ArrayList()

    fun readObjects(stream: InputStream) = DataInputStream(stream).use {
        var id: Short = 0
        var eventId: Byte = 0
        stream.skip(14) // Skip header and object counts
        while (true) {
            val obj = GameObjectRegistry.read(it) ?: break
            obj.id = id++
            println("Read object with id ${obj.id} and type ${obj.type}")
            if (obj.type == 6) {
                (obj as Event).eventId = eventId++
            }
            objects.add(obj)
        }
    }

    fun writeObjects(stream: OutputStream): Int {
        val dataStream = DataOutputStream(stream)
        dataStream.write(FILE_HEADER)
        dataStream.writeShort(objects.size)
        dataStream.writeShort(0) // Unused
        dataStream.writeShort(objects.count { obj -> obj.type == 6 })
        var bytesWritten = 14
        for (obj in objects) {
            bytesWritten += GameObjectRegistry.write(dataStream, obj)
        }
        dataStream.writeByte(127)
        return bytesWritten + 1
    }

    companion object {
        val FILE_HEADER: ByteArray = byteArrayOf(0x52, 0x4C, 0x45, 0x46, 0x00, 0x01, 0x00, 0x00)
    }
}
