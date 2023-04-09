package dev.storozhenko.familybot.feature.tribute.repos

import dev.storozhenko.familybot.common.extensions.readTomlFromStatic
import dev.storozhenko.familybot.core.telegram.FamilyBot
import org.springframework.stereotype.Component
import org.tomlj.TomlTable

@Component
class QuoteRepository {
    private val quotes: Map<String, List<String>>
    private val flattenQuotes: List<String>

    init {
        val toml = readTomlFromStatic("quotes.toml")

        val rawArray = toml.getArray("quotes")
            ?: throw FamilyBot.InternalException("quotes.toml is missing quotes array")
        quotes = rawArray.toList()
            .map { row -> map(row as TomlTable) }
            .groupBy(Pair<String, String>::first, Pair<String, String>::second)
        flattenQuotes = quotes.values.flatten()
    }

    private fun map(row: TomlTable): Pair<String, String> {
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
