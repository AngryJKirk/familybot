package space.yaroslav.familybot.repos


interface KeywordRepository {

    fun getKeywords(): Set<String>

    fun getPhrasesByKeyword(key: String): Set<String>

}