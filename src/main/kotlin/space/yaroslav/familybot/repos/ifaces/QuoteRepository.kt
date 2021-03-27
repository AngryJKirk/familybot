package space.yaroslav.familybot.repos.ifaces

import io.micrometer.core.annotation.Timed

interface QuoteRepository {

    @Timed("QuoteRepository.getByTag")
    fun getByTag(tag: String): String?

    @Timed("QuoteRepository.getRandom")
    fun getRandom(): String

    @Timed("QuoteRepository.getTags")
    fun getTags(): List<String>
}
