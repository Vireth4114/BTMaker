package objects.egg

import objects.GameObjectWriter
import java.io.DataOutput

class EggWriter: GameObjectWriter<Egg>() {
    override fun writeSpecific(output: DataOutput, obj: Egg) {
        // No specific data to write for Egg
    }
}