package space.yaroslav.familybot.repos

import space.yaroslav.familybot.controllers.QuoteDTO


interface QuoteRepository {

    fun getByTag(tag: String): String?

    fun getRandom(): String

    fun addQuote(quote: QuoteDTO)
}