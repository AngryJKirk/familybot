package space.yaroslav.familybot.services.talking

import org.springframework.stereotype.Component
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.repos.PhraseDictionaryRepository
import space.yaroslav.familybot.repos.PhraseThemeSetting
import java.time.Instant

@Component
class Dictionary(private val dictionaryRepository: PhraseDictionaryRepository) {
    fun getAll(phrase: Phrase): List<String> {
        return dictionaryRepository.getAllPhrases(phrase)
    }

    fun get(phrase: Phrase): String {
        val now = Instant.now()
        val theme = dictionaryRepository.getPhraseSettings()
            .find { isCurrentSetting(now, it) }
            ?.theme
            ?: dictionaryRepository.getDefaultPhraseTheme()

        return dictionaryRepository.getPhrases(phrase, theme).random()
    }

    private fun isCurrentSetting(
        now: Instant,
        it: PhraseThemeSetting
    ) = now.isAfter(it.since) and now.isBefore(it.till)
}
