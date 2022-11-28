package dev.storozhenko.familybot.models.dictionary

enum class Pluralization {
    ONE,
    FEW,
    MANY;

    companion object PluralizationCalc {
        fun getPlur(position: Int) = getPlur(position.toLong())

        fun getPlur(position: Long): Pluralization {
            return if (position % 10 == 1L && position % 100 != 11L) ONE
            else if (position % 10 in 2..4 && (position % 100 < 10 || position % 100 >= 20)) FEW
            else MANY
        }
    }
}
