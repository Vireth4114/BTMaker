package objects

enum class ObjectType(val id: Int) {
    GEOMETRY(4),
    EVENT(6),
    BOUNCE(8),
    SPRITE(9),
    WATER(10),
    CANNON(11),
    TRAMPOLINE(12),
    EGG(13),
    ENEMY(15);

    companion object {
        fun fromId(id: Int): ObjectType? =
            entries.find { it.id == id }
    }
}