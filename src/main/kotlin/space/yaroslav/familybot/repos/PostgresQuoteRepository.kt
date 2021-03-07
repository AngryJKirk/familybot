package space.yaroslav.familybot.repos

import com.google.common.base.Suppliers
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.repos.ifaces.QuoteRepository
import java.util.concurrent.TimeUnit

@Component
@Primary
class PostgresQuoteRepository(val template: JdbcTemplate) : QuoteRepository {

    private val quoteCache = Suppliers.memoizeWithExpiration(
        { template.query("SELECT * FROM quotes") { rs, _ -> rs.getString("quote") } },
        5,
        TimeUnit.MINUTES
    )

    private val byTagCache = CacheBuilder.newBuilder().expireAfterWrite(3, TimeUnit.MINUTES).build(
        CacheLoader.from { tag: String? ->
            template.query(
                "SELECT * FROM quotes INNER JOIN tags2quotes t2q ON quotes.id = t2q.quote_id " +
                    "WHERE t2q.tag_id = (SELECT id from tags WHERE LOWER(tag) = lower(?))",
                { rs, _ -> rs.getString("quote") },
                tag
            )
        }
    )

    override fun getTags(): List<String> {
        return template.queryForList("SELECT tag FROM tags", String::class.java)
    }

    override fun getByTag(tag: String): String? {
        return byTagCache.get(tag).random()
    }

    override fun getRandom(): String {
        return quoteCache.get().random()
    }
}
