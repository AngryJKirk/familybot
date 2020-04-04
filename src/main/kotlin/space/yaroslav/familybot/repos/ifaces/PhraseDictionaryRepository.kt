package space.yaroslav.familybot.repos.ifaces

import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.models.PhraseTheme
import space.yaroslav.familybot.repos.PhraseThemeSetting

interface PhraseDictionaryRepository {

    fun getPhraseTheme(chat: Chat): PhraseTheme

    fun getDefaultPhraseTheme(): PhraseTheme

    fun getPhraseSettings(): List<PhraseThemeSetting>

    fun getPhrases(phrase: Phrase, phraseTheme: PhraseTheme): List<String>

    fun getAllPhrases(phrase: Phrase): List<String>
}
