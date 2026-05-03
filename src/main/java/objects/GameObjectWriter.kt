package objects

import java.io.DataOutput

interface GameObjectWriter<T: GameObject> {
    fun write(output: DataOutput, objectToWrite: T)
}