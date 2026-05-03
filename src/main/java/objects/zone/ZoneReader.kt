package objects.zone

import objects.GameObjectReader
import java.io.DataInput

abstract class ZoneReader<T: Zone>: GameObjectReader<T>() {
    override fun readSpecific(input: DataInput, obj: T) = obj.run {
        minX = input.readShort()
        maxY = input.readShort()
        maxX = input.readShort()
        minY = input.readShort()

        readSpecificZone(input, this)
    }

    abstract fun readSpecificZone(input: DataInput, obj: T)
}