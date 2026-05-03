package objects.enemy

import objects.GameObjectReader
import java.io.DataInput

class EnemyReader: GameObjectReader<Enemy>() {
    override fun createInstance() = Enemy()

    override fun readSpecific(input: DataInput, obj: Enemy) = obj.run {
        startX = input.readShort()
        startY = input.readShort()
        endX = input.readShort()
        endY = input.readShort()
        enemyType = EnemyType.fromValue(input.readByte())
    }
}