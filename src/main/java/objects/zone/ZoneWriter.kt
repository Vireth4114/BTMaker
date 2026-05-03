package objects.zone

import objects.GameObjectWriter
import java.io.DataOutput

abstract class ZoneWriter<T: Zone>: GameObjectWriter<T> {
    override fun write(output: DataOutput, objectToWrite: T) {

    }
}