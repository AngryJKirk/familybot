package space.yaroslav.familybot.repos.ifaces


interface PidorDictionaryRepository {

    fun getStart(): List<String>

    fun getMiddle(): List<String>

    fun getFinish(): List<String>

}