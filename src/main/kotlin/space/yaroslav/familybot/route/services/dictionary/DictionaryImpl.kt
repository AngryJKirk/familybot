package space.yaroslav.familybot.route.services.dictionary

import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.utils.randomNotNull
import space.yaroslav.familybot.repos.ifaces.PhraseDictionaryRepository
import space.yaroslav.familybot.route.models.Phrase
import java.time.Instant

@Component
class DictionaryImpl(val dictionaryRepository: PhraseDictionaryRepository) : Dictionary {
    override fun getAll(phrase: Phrase): List<String> {
        return dictionaryRepository.getAllPhrases(phrase)
    }

    override fun get(phrase: Phrase): String {
        val now = Instant.now()
        val theme = dictionaryRepository.getPhraseSettings()
            .find { now.isAfter(it.since) and now.isBefore(it.till) }
            ?.theme
            ?: dictionaryRepository.getDefaultPhraseTheme()

        return dictionaryRepository.getPhrases(phrase, theme).randomNotNull()
    }
}
