package objects.cannon

import objects.GameObjectReader
import java.io.DataInput

class CannonReader: GameObjectReader<Cannon>() {
    override fun createInstance() = Cannon()

    override fun readSpecific(input: DataInput, obj: Cannon) = obj.run {
        playerId = input.readShort()
        power = input.readByte()
    }
}