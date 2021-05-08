package space.yaroslav.familybot.models.dictionary

enum class Pluralization {
    ONE,
    FEW,
    MANY;

    companion object PluralizationCalc {
        fun getPlur(position: Int): Pluralization {
            return if (position % 10 == 1 && position % 100 != 11) ONE
            else if (position % 10 in 2..4 && (position % 100 < 10 || position % 100 >= 20)) FEW
            else MANY
        }
    }
}