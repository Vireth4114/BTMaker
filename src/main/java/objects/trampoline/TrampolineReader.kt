package objects.trampoline

import objects.GameObjectReader
import java.io.DataInput

class TrampolineReader: GameObjectReader<Trampoline>() {
    override fun createInstance() = Trampoline()

    override fun readSpecific(input: DataInput, obj: Trampoline) = obj.run {
        imageId = input.readShort()
        pushForce = input.readByte()
    }
}