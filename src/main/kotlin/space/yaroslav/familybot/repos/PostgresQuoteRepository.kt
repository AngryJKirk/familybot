package space.yaroslav.familybot.repos

import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.random
import space.yaroslav.familybot.controllers.QuoteDTO
import space.yaroslav.familybot.repos.ifaces.QuoteRepository

@Component
@Primary
class PostgresQuoteRepository(val template: JdbcTemplate) : QuoteRepository {


    override fun getTags(): List<String> {
        return template.queryForList("SELECT tag from tags", String::class.java)
    }


    override fun addQuote(quote: QuoteDTO) {
        val quoteIds = quote.tags.map { addTag(it) }
        val quoteId = addQuote(quote.quote)
        quoteIds.forEach { addQuoteToTag(it, quoteId) }
    }

    override fun getByTag(tag: String): String? {
        return template.query("SELECT * FROM quotes INNER JOIN tags2quotes t2q ON quotes.id = t2q.quote_id " +
                "WHERE t2q.tag_id = (SELECT id from tags WHERE LOWER(tag) = lower('$tag'))",
                { rs, _ -> rs.getString("quote") }).random()
    }

    override fun getRandom(): String {
        return template.query("SELECT * FROM quotes", { rs, _ -> rs.getString("quote") }).random()!!
    }

    private fun addTag(tag: String): Int {
        return template.queryForObject("INSERT INTO tags (tag, chat_id) VALUES (lower('$tag'), -1001094220065) ON CONFLICT(tag) DO UPDATE SET tag = lower('$tag') RETURNING id", Int::class.java)
    }

    private fun addQuote(quote: String): Int {
        return template.queryForObject("INSERT INTO quotes (quote) VALUES ('$quote') RETURNING id"
        , Int::class.java)
    }

    private fun addQuoteToTag(tagId : Int, quoteId: Int){
        template.update("INSERT INTO tags2quotes (tag_id, quote_id) VALUES ($tagId, $quoteId)")
    }
}