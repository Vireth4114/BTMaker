package BTMaker.BTMaker.resources

enum class SpriteType {
    SIMPLE,
    COMPOUND,
    ANIMATED;

    companion object {
        fun fromCode(code: Number): SpriteType {
            return when (code) {
                0 -> SIMPLE
                1 -> COMPOUND
                2 -> ANIMATED
                else -> throw IllegalArgumentException("Unknown resource type code: $code")
            }
        }
    }
}