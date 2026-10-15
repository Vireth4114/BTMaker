package objects.egg

import objects.GameObjectReader
import java.io.DataInput

object EggReader: GameObjectReader<Egg>() {
    override fun createInstance() = Egg()

    override fun readSpecific(input: DataInput, obj: Egg) {
        // No specific data to read for Egg
    }
}