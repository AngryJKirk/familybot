package space.yaroslav.familybot.repos

import org.springframework.stereotype.Component
import org.tomlj.Toml
import org.tomlj.TomlTable
import space.yaroslav.familybot.telegram.FamilyBot

@Component
class QuoteRepository {
    private val quotes: Map<String, List<String>>
    private val flattenQuotes: List<String>

    init {
        val resourceAsStream = this::class.java.classLoader
            .getResourceAsStream("static/quotes.toml")
            ?: throw FamilyBot.InternalException("quotes.toml is missing")

        val toml = Toml.parse(resourceAsStream)
        val rawArray = toml.getArray("quotes")
            ?: throw FamilyBot.InternalException("quotes.toml is missing quotes array")
        quotes = rawArray.toList()
            .map { row -> row as TomlTable }
            .map { row -> function(row) }
            .groupBy(Pair<String, String>::first, Pair<String, String>::second)
        flattenQuotes = quotes.values.flatten()
    }

    private fun function(row: TomlTable): Pair<String, String> {
        val tag = row["tag"] as String?
        val quote = row["quote"] as String?
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
