package objects.enemy

enum class EnemyType(val value: Int) {
    MOLE(0),
    SPINNER(2);

    companion object {
        fun fromValue(value: Number) = entries.firstOrNull { it.value == value.toInt() }
    }
}