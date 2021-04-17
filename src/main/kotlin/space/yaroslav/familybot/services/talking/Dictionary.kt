package space.yaroslav.familybot.services.talking

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.utils.key
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.models.PhraseTheme
import space.yaroslav.familybot.models.UkranianLanguage
import space.yaroslav.familybot.repos.PhraseDictionaryRepository
import space.yaroslav.familybot.repos.PhraseThemeSetting
import space.yaroslav.familybot.services.settings.EasySettingsService
import space.yaroslav.familybot.services.settings.SettingsKey
import java.time.Instant

@Component
class Dictionary(
    private val dictionaryRepository: PhraseDictionaryRepository,
    private val settingsService: EasySettingsService
) {
    fun getAll(phrase: Phrase): List<String> {
        return dictionaryRepository.getAllPhrases(phrase)
    }

    fun createContext(update: Update): DictionaryContext {
        return DictionaryContext(update.toChat().key(), this::getInternal)
    }

    fun createContext(chat: Chat): DictionaryContext {
        return DictionaryContext(chat.key(), this::getInternal)
    }

    fun get(phrase: Phrase, update: Update) = getInternal(phrase, update.toChat().key())

    fun get(phrase: Phrase, settingsKey: SettingsKey) = getInternal(phrase, settingsKey)

    fun getInternal(phrase: Phrase, settingsKey: SettingsKey): String {
        val isUkrainian = settingsService.get(UkranianLanguage, settingsKey)
        val now = Instant.now()
        val theme = if (isUkrainian == true) {
            PhraseTheme.UKRAINIAN
        } else {
            dictionaryRepository.getPhraseSettings()
                .find { isCurrentSetting(now, it) }
                ?.theme
                ?: dictionaryRepository.getDefaultPhraseTheme()
        }

        return dictionaryRepository.getPhrases(phrase, theme).random()
    }

    private fun isCurrentSetting(
        now: Instant,
        it: PhraseThemeSetting
    ) = now.isAfter(it.since) and now.isBefore(it.till)
}

class DictionaryContext(
    private val settingsKey: SettingsKey,
    private val callback: (Phrase, SettingsKey) -> String
) {
    fun get(phrase: Phrase) = callback(phrase, settingsKey)
}
