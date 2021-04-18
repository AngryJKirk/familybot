package space.yaroslav.familybot.repos

import com.moandjiezana.toml.Toml
import org.springframework.stereotype.Component
import space.yaroslav.familybot.telegram.FamilyBot

@Component
class QuoteRepository {
    private val quotes: Map<String, List<String>>
    private val flattenQuotes: List<String>

    init {
        val toml = Toml().read(this::class.java.classLoader.getResourceAsStream("static/quotes.toml"))
        quotes = toml.getList<Map<String, String>>("quotes")
            .map { row -> function(row) }
            .groupBy(Pair<String, String>::first, Pair<String, String>::second)
        flattenQuotes = quotes.values.flatten()
    }

    private fun function(row: Map<String, String>): Pair<String, String> {
        val tag = row["tag"]
        val quote = row["quote"]
        if (tag == null || quote == null) {
            throw FamilyBot.InternalException("quotes.toml is invalid, current row is $row")
        }
        return tag to quote
    }

    fun getTags(): Set<String> {
        return quotes.keys
    }

    fun getByTag(tag: String): String? {
        return quotes[tag]?.random()
    }

    fun getRandom(): String {
        return flattenQuotes.random()
    }
}
