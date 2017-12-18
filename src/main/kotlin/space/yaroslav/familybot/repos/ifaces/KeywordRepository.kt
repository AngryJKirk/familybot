package space.yaroslav.familybot.repos.ifaces


interface KeywordRepository {

    fun getKeywords(): Set<String>

    fun getPhrasesByKeyword(key: String): Set<String>

}