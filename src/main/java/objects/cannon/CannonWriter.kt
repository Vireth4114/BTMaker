package objects.cannon

import objects.GameObjectWriter
import java.io.DataOutput

class CannonWriter: GameObjectWriter<Cannon>() {
    override fun writeSpecific(output: DataOutput, obj: Cannon) = output.run {
        writeShort(obj.playerId.toInt())
        writeByte(obj.power.toInt())
    }
}