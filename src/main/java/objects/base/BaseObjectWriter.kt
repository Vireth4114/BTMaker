package objects.base

import objects.GameObject
import objects.GameObjectWriter
import java.io.DataOutput

object BaseObjectWriter: GameObjectWriter<GameObject>() {
    override fun writeSpecific(output: DataOutput, obj: GameObject) {
        // No specific data to write for BaseObject
    }
}