package objects.bounce

import objects.GameObjectWriter
import java.io.DataOutput

class BounceWriter: GameObjectWriter<Bounce>() {
    override fun writeSpecific(output: DataOutput, obj: Bounce) {
        // No specific data to write for Bounce
    }
}