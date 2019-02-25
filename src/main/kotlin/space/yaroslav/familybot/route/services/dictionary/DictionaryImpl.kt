package space.yaroslav.familybot.route.services.dictionary

import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.utils.random
import space.yaroslav.familybot.repos.ifaces.PhraseDictionaryRepository
import space.yaroslav.familybot.route.models.Phrase

@Component
class DictionaryImpl(val dictionaryRepository: PhraseDictionaryRepository) : Dictionary {
    override fun getAll(phrase: Phrase): List<String> {
        return dictionaryRepository.getAllPhrases(phrase)
    }

    override fun get(phrase: Phrase): String {
        return dictionaryRepository.getPhrases(phrase, dictionaryRepository.getDefaultPhraseTheme()).random()!!
    }
}
