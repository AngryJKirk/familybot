package space.yaroslav.familybot.repos.ifaces


interface PidorDictionaryRepository {

    fun getStart(): List<String>

    fun getMiddle(): List<String>

    fun getFinish(): List<String>

    fun getLeaderBoardPhrase(pluralization: Pluralization): List<String>

}

enum class Pluralization(val code: Int) {
    ONE(1),
    FEW(2),
    MANY(3);

    companion object PluralizationCalc {
        fun getPlur(position: Int): Pluralization {
            return if (position % 10 == 1 && position % 100 != 11) ONE
            else if (position % 10 in 2..4 && (position % 100 < 10 || position % 100 >= 20)) FEW
            else MANY
        }
    }

}