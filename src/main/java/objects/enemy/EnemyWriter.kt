package objects.enemy

import objects.GameObjectWriter
import java.io.DataOutput

class EnemyWriter: GameObjectWriter<Enemy>() {
    override fun writeSpecific(output: DataOutput, obj: Enemy) = output.run {
        writeShort(obj.startX.toInt())
        writeShort(obj.startY.toInt())
        writeShort(obj.endX.toInt())
        writeShort(obj.endY.toInt())
        writeByte(obj.enemyType?.value ?: 0)
    }
}