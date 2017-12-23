package space.yaroslav.familybot.repos

import com.google.common.base.Suppliers
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.random
import space.yaroslav.familybot.controllers.QuoteDTO
import space.yaroslav.familybot.repos.ifaces.QuoteRepository
import java.util.concurrent.TimeUnit

@Component
@Primary
class PostgresQuoteRepository(val template: JdbcTemplate) : QuoteRepository {


    private val quoteCache = Suppliers.memoizeWithExpiration(
            { template.query("SELECT * FROM quotes", { rs, _ -> rs.getString("quote") }) },
            5,
            TimeUnit.MINUTES)

    private val byTagCache = CacheBuilder.newBuilder().expireAfterWrite(3, TimeUnit.MINUTES).build(
            CacheLoader.from({ tag: String? -> template.query("SELECT * FROM quotes INNER JOIN tags2quotes t2q ON quotes.id = t2q.quote_id " +
                    "WHERE t2q.tag_id = (SELECT id from tags WHERE LOWER(tag) = lower('$tag'))",
                    { rs, _ -> rs.getString("quote") })})
    )

    override fun getTags(): List<String> {
        return template.queryForList("SELECT tag FROM tags", String::class.java)
    }


    override fun addQuote(quote: QuoteDTO) {
        val quoteIds = quote.tags.map { addTag(it) }
        val quoteId = addQuote(quote.quote)
        quoteIds.forEach { addQuoteToTag(it, quoteId) }
    }

    override fun getByTag(tag: String): String? {
        return byTagCache.get(tag).random()
    }

    override fun getRandom(): String {
        return quoteCache.get().random()!!
    }

    private fun addTag(tag: String): Int {
        return template.queryForObject("INSERT INTO tags (tag, chat_id) VALUES (lower('$tag'), -1001094220065) ON CONFLICT(tag) DO UPDATE SET tag = lower('$tag') RETURNING id", Int::class.java)
    }

    private fun addQuote(quote: String): Int {
        return template.queryForObject("INSERT INTO quotes (quote) VALUES ('$quote') RETURNING id"
                , Int::class.java)
    }

    private fun addQuoteToTag(tagId: Int, quoteId: Int) {
        template.update("INSERT INTO tags2quotes (tag_id, quote_id) VALUES ($tagId, $quoteId)")
    }
}