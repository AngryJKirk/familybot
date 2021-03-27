package space.yaroslav.familybot.repos.ifaces

import io.micrometer.core.annotation.Timed
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.models.PhraseTheme
import space.yaroslav.familybot.repos.PhraseThemeSetting

interface PhraseDictionaryRepository {

    @Timed("PhraseDictionaryRepository.getPhraseTheme")
    fun getPhraseTheme(chat: Chat): PhraseTheme

    @Timed("PhraseDictionaryRepository.getDefaultPhraseTheme")
    fun getDefaultPhraseTheme(): PhraseTheme

    @Timed("PhraseDictionaryRepository.getPhraseSettings")
    fun getPhraseSettings(): List<PhraseThemeSetting>

    @Timed("PhraseDictionaryRepository.getPhrases")
    fun getPhrases(phrase: Phrase, phraseTheme: PhraseTheme): List<String>

    @Timed("PhraseDictionaryRepository.getAllPhrases")
    fun getAllPhrases(phrase: Phrase): List<String>
}
