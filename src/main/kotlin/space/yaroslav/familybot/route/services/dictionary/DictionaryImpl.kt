package space.yaroslav.familybot.route.services.dictionary

import java.time.Instant
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.utils.randomNotNull
import space.yaroslav.familybot.repos.PhraseThemeSetting
import space.yaroslav.familybot.repos.ifaces.PhraseDictionaryRepository
import space.yaroslav.familybot.route.models.Phrase

@Component
class DictionaryImpl(private val dictionaryRepository: PhraseDictionaryRepository) : Dictionary {
    override fun getAll(phrase: Phrase): List<String> {
        return dictionaryRepository.getAllPhrases(phrase)
    }

    override fun get(phrase: Phrase): String {
        val now = Instant.now()
        val theme = dictionaryRepository.getPhraseSettings()
            .find { isCurrentSetting(now, it) }
            ?.theme
            ?: dictionaryRepository.getDefaultPhraseTheme()

        return dictionaryRepository.getPhrases(phrase, theme).randomNotNull()
    }

    private fun isCurrentSetting(
        now: Instant,
        it: PhraseThemeSetting
    ) = now.isAfter(it.since) and now.isBefore(it.till)
}
