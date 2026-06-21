package objects.trampoline

import objects.GameObjectWriter
import java.io.DataOutput

class TrampolineWriter: GameObjectWriter<Trampoline>() {
    override fun writeSpecific(output: DataOutput, obj: Trampoline) = output.run {
        writeShort(obj.imageId.toInt())
        writeByte(obj.pushForce.toInt())
    }
}