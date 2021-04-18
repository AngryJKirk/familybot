package space.yaroslav.familybot.services.talking

import com.moandjiezana.toml.Toml
import org.springframework.stereotype.Component
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.models.PhraseTheme
import space.yaroslav.familybot.telegram.FamilyBot

@Component
class DictionaryReader {
    private val dictionary: Map<Phrase, Map<PhraseTheme, List<String>>>

    init {
        val toml = Toml().read(this::class.java.classLoader.getResourceAsStream("dictionary.toml"))
        dictionary = Phrase.values()
            .map { phrase -> phrase to toml.getTable(phrase.name) }
            .associate { (phrase, table) -> phrase to parsePhrasesByTheme(table) }
        checkDefaults(dictionary)
    }

    fun getPhrases(phrase: Phrase, theme: PhraseTheme): List<String> {
        val byThemes = dictionary[phrase]
            ?: throw FamilyBot.InternalException("Phrase $phrase is missing")
        return byThemes[theme] ?: byThemes[PhraseTheme.DEFAULT]
        ?: throw FamilyBot.InternalException("Default value for phrase $phrase is missing")
    }

    fun getAllPhrases(phrase: Phrase): List<String> {
        val byThemes = (dictionary[phrase]
            ?: throw FamilyBot.InternalException("Phrase $phrase is missing"))
        return byThemes.map(Map.Entry<PhraseTheme, List<String>>::value).flatten()
    }

    private fun parsePhrasesByTheme(table: Toml): Map<PhraseTheme, List<String>> {
        return PhraseTheme
            .values()
            .associate { theme -> theme to (table.getList(theme.name) ?: emptyList()) }
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