package space.yaroslav.familybot.repos.ifaces

interface QuoteRepository {

    fun getByTag(tag: String): String?

    fun getRandom(): String

    fun getTags(): List<String>
}
