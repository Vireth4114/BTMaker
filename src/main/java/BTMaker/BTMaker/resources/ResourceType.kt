package BTMaker.BTMaker.resources

enum class ResourceType {
    IMAGE,
    MIDI,
    STRINGS,
    LAYOUT,
    LEVEL;

    companion object {
        fun fromCode(code: Number): ResourceType {
            return when (code) {
                2 -> IMAGE
                3 -> MIDI
                4 -> STRINGS
                5 -> LAYOUT
                8 -> LEVEL
                else -> throw IllegalArgumentException("Unknown resource type code: $code")
            }
        }
    }
}