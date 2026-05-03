package objects.bounce

import objects.GameObjectReader
import java.io.DataInput

class BounceReader: GameObjectReader<Bounce>() {
    override fun createInstance() = Bounce()

    override fun readSpecific(input: DataInput, obj: Bounce) {
        // No specific data to read for Bounce
    }
}