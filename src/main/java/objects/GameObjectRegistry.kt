package objects

import objects.base.BaseObject
import objects.bounce.BounceReader
import objects.bounce.BounceWriter
import objects.cannon.CannonReader
import objects.cannon.CannonWriter
import objects.egg.EggReader
import objects.egg.EggWriter
import objects.enemy.EnemyReader
import objects.enemy.EnemyWriter
import objects.event.EventReader
import objects.event.EventWriter
import objects.geometry.GeometryReader
import objects.geometry.GeometryWriter
import objects.base.BaseObjectReader
import objects.base.BaseObjectWriter
import objects.sprite.SpriteReader
import objects.sprite.SpriteWriter
import objects.trampoline.TrampolineReader
import objects.trampoline.TrampolineWriter
import objects.water.WaterReader
import objects.water.WaterWriter
import java.io.DataInputStream
import java.io.DataOutputStream

object GameObjectRegistry {
    val readers = mutableMapOf<Int, (DataInputStream) -> GameObject>()
    val writers = mutableMapOf<Int, (DataOutputStream, GameObject) -> Int>()

    inline fun <reified T: GameObject> register(type: Int, reader: GameObjectReader<T>, writer: GameObjectWriter<T>) {
        readers[type] = { input: DataInputStream -> reader.read(input) }
        writers[type] = { out: DataOutputStream, obj: GameObject -> writer.write(out, obj as T) }
    }

    init {
        register(4, GeometryReader(), GeometryWriter())
        register(6, EventReader(), EventWriter())
        register(8, BounceReader(), BounceWriter())
        register(9, SpriteReader(), SpriteWriter())
        register(10, WaterReader(), WaterWriter())
        register(11, CannonReader(), CannonWriter())
        register(12, TrampolineReader(), TrampolineWriter())
        register(13, EggReader(), EggWriter())
        register(15, EnemyReader(), EnemyWriter())
    }

    fun read(input: DataInputStream): GameObject? {
        val type = input.readByte().toInt()
        if (type == 127) {
            return null
        }
        val reader = readers[type] ?: BaseObjectReader()::read
        return reader(input)
    }

    fun write(output: DataOutputStream, obj: GameObject): Int {
        output.writeByte(obj.type)
        val writer = writers[obj.type] ?: { out, obj -> BaseObjectWriter().write(out, obj as BaseObject) }
        return writer(output, obj) + 1
    }
}