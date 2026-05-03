package objects

import btmaker.CountingDataInputStream
import java.io.DataInput

abstract class GameObjectReader<T : GameObject> {
    fun read(input: CountingDataInputStream): T {
        val obj = createInstance()

        val length = input.readShort()
        input.chunk(length) {
            readCommon(input, obj)
            readSpecific(input, obj)
        }

        return obj
    }

    protected abstract fun createInstance(): T

    private fun readCommon(input: DataInput, obj: GameObject) = obj.run {
        parentID = input.readShort()
        previousID = input.readShort()

        val transformFlags = input.readByte().toInt()
        if ((transformFlags and 7) == 7) {
            val a = input.readInt() / 65536.0
            val b = input.readInt() / 65536.0
            xPos = input.readInt() shr 16
            val c = input.readInt() / 65536.0
            val d = input.readInt() / 65536.0
            yPos = input.readInt() shr 16
            setScaleAndRotationFromMatrix(a, b, c, d)
        } else {
            if ((transformFlags and 1) > 0) {
                xPos = input.readShort()
                yPos = input.readShort()
            }
            if ((transformFlags and 2) > 0) {
                rotation = input.readInt() / 65536.0
            }
            if ((transformFlags and 4) > 0) {
                xScale = input.readInt() / 65536.0
                yScale = input.readInt() / 65536.0
            }
        }

        val flags = input.readInt()
        zIndex = flags and 0x1f
        noDraw = (flags and 0x80) > 0
        noCollision = (flags and 0x20) > 0
    }

    protected abstract fun readSpecific(input: DataInput, obj: T)
}