package objects

import objects.base.*
import objects.bounce.*
import objects.cannon.*
import objects.egg.*
import objects.enemy.*
import objects.event.*
import objects.geometry.*
import objects.sprite.*
import objects.trampoline.*
import objects.water.*
import java.io.DataInputStream
import java.io.DataOutputStream

object ObjectIOFacade {
    fun read(input: DataInputStream): GameObject? {
        val type = input.readByte().toInt()
        if (type == 127) return null

        val reader = when (ObjectType.fromId(type)) {
            ObjectType.GEOMETRY -> GeometryReader
            ObjectType.EVENT -> EventReader
            ObjectType.BOUNCE -> BounceReader
            ObjectType.SPRITE -> SpriteReader
            ObjectType.WATER -> WaterReader
            ObjectType.CANNON -> CannonReader
            ObjectType.TRAMPOLINE -> TrampolineReader
            ObjectType.EGG -> EggReader
            ObjectType.ENEMY -> EnemyReader
            else -> BaseObjectReader
        }

        return reader.read(input)
    }

    fun write(output: DataOutputStream, obj: GameObject): Int {
        output.writeByte(obj.type)
        return 1 + when (obj) {
            is Geometry -> GeometryWriter.write(output, obj)
            is Event -> EventWriter.write(output, obj)
            is Bounce -> BaseObjectWriter.write(output, obj)
            is Sprite -> SpriteWriter.write(output, obj)
            is Water -> WaterWriter.write(output, obj)
            is Cannon -> CannonWriter.write(output, obj)
            is Trampoline -> TrampolineWriter.write(output, obj)
            is Egg -> BaseObjectWriter.write(output, obj)
            is Enemy -> EnemyWriter.write(output, obj)
            else -> BaseObjectWriter.write(output, obj)
        }
    }
}