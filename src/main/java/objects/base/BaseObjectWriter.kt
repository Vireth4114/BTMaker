package objects.base

import objects.GameObjectWriter
import java.io.DataOutput

class BaseObjectWriter: GameObjectWriter<BaseObject>() {
    override fun writeSpecific(output: DataOutput, obj: BaseObject) {
        // No specific data to write for BaseObject
    }
}