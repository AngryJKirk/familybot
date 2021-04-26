package space.yaroslav.familybot.services.talking

import org.springframework.stereotype.Component
import org.tomlj.Toml
import org.tomlj.TomlTable
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.models.PhraseTheme
import space.yaroslav.familybot.telegram.FamilyBot
@Component
class DictionaryReader {
    private val dictionary: Map<Phrase, Map<PhraseTheme, List<String>>>

    init {
        val resourceAsStream = this::class.java.classLoader
            .getResourceAsStream("static/dictionary.toml")
            ?: throw FamilyBot.InternalException("dictionary.toml is missing")

        val toml = Toml.parse(resourceAsStream)

        dictionary = Phrase.values()
            .map { phrase ->
                phrase to (toml.getTable(phrase.name) ?: throw FamilyBot.InternalException("Phrase $phrase is missing"))
            }
            .associate { (phrase, table) -> phrase to parsePhrasesByTheme(table) }
        checkDefaults(dictionary)
    }

    fun getPhrases(phrase: Phrase, theme: PhraseTheme): List<String> {
        val phrasesByTheme = getPhraseContent(phrase)
        val requiredPhrases = phrasesByTheme[theme]
        return if (requiredPhrases.isNullOrEmpty()) {
            phrasesByTheme[PhraseTheme.DEFAULT]
                ?: throw FamilyBot.InternalException("Default value for phrase $phrase is missing")
        } else {
            requiredPhrases
        }
    }

    fun getAllPhrases(phrase: Phrase): List<String> {
        val byThemes = getPhraseContent(phrase)
        return byThemes.map(Map.Entry<PhraseTheme, List<String>>::value).flatten()
    }

    private fun getPhraseContent(phrase: Phrase): Map<PhraseTheme, List<String>> {
        return dictionary[phrase]
            ?: throw FamilyBot.InternalException("Phrase $phrase is missing")
    }

    private fun parsePhrasesByTheme(table: TomlTable): Map<PhraseTheme, List<String>> {
        return PhraseTheme
            .values()
            .associate { theme -> theme to tableToList(table, theme) }
    }

    private fun tableToList(
        table: TomlTable,
        theme: PhraseTheme
    ): List<String> {
        return table
            .getArray(theme.name)
            ?.toList()
            ?.map(Any::toString)
            ?.map { line -> line.replace("\r", "") }
            ?: emptyList()
    }

    private fun checkDefaults(dictionary: Map<Phrase, Map<PhraseTheme, List<String>>>) {
        val missingDefaultPhrases = dictionary
            .filter { entry -> entry.value[PhraseTheme.DEFAULT].isNullOrEmpty() }
            .map { entry -> entry.key }

        if (missingDefaultPhrases.isNotEmpty()) {
            throw FamilyBot.InternalException(
                "Some dictionary defaults missing. " +
                    "Check $missingDefaultPhrases"
            )
        }
    }
}
