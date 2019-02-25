package space.yaroslav.familybot.repos.ifaces

import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.models.PhraseTheme

interface PhraseDictionaryRepository {

    fun getPhraseTheme(chat: Chat): PhraseTheme

    fun getDefaultPhraseTheme(): PhraseTheme

    fun getPhrases(phrase: Phrase, phraseTheme: PhraseTheme): List<String>

    fun getAllPhrases(phrase: Phrase): List<String>
}
