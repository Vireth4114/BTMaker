package objects.zone

import objects.GameObjectWriter
import java.io.DataOutput

abstract class ZoneWriter<T: Zone>: GameObjectWriter<T>() {
    override fun writeSpecific(output: DataOutput, obj: T) = output.run {
        writeShort(obj.minX.toInt())
        writeShort(obj.maxY.toInt())
        writeShort(obj.maxX.toInt())
        writeShort(obj.minY.toInt())

        writeSpecificZone(this, obj)
    }

    abstract fun writeSpecificZone(output: DataOutput, obj: T)
}