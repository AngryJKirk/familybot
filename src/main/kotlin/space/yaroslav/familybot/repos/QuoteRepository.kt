package space.yaroslav.familybot.repos


interface QuoteRepository {

    fun getByTag(tag: String): String?

    fun getRandom(): String
}