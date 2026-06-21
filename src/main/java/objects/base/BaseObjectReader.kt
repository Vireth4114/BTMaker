package objects.base

import objects.GameObjectReader
import java.io.DataInput

class BaseObjectReader: GameObjectReader<BaseObject>() {
    override fun createInstance() = BaseObject()

    override fun readSpecific(input: DataInput, obj: BaseObject) {
        // No specific data to read for BaseObject
    }
}