package space.yaroslav.familybot.services.talking

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.utils.key
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.models.PhraseTheme
import space.yaroslav.familybot.services.settings.ChatSettingsKey
import space.yaroslav.familybot.services.settings.EasySettingsService
import space.yaroslav.familybot.services.settings.UkrainianLanguage
import java.time.LocalDate
import java.time.Month

@Component
class Dictionary(
    private val settingsService: EasySettingsService,
    private val dictionaryReader: DictionaryReader
) {
    fun getAll(phrase: Phrase): List<String> {
        return dictionaryReader.getAllPhrases(phrase)
    }

    fun createContext(update: Update): DictionaryContext {
        return DictionaryContext(update.toChat().key(), this::getInternal)
    }

    fun createContext(chat: Chat): DictionaryContext {
        return DictionaryContext(chat.key(), this::getInternal)
    }

    fun get(phrase: Phrase, update: Update) = getInternal(phrase, update.toChat().key())

    fun get(phrase: Phrase, settingsKey: ChatSettingsKey) = getInternal(phrase, settingsKey)

    fun getInternal(phrase: Phrase, settingsKey: ChatSettingsKey): String {
        val isUkrainian = settingsService.get(UkrainianLanguage, settingsKey)
        val theme = if (isUkrainian == true) {
            PhraseTheme.UKRAINIAN
        } else {
            getHolidayTheme() ?: PhraseTheme.DEFAULT
        }

        return dictionaryReader.getPhrases(phrase, theme).random()
    }

    private fun getHolidayTheme(): PhraseTheme? {
        val now = LocalDate.now()
        if (now.month == Month.MARCH && now.dayOfMonth == 8) {
            return PhraseTheme.DAY_OF_WOMAN_8_MARCH
        }
        if (now.month == Month.FEBRUARY && now.dayOfMonth == 23) {
            return PhraseTheme.DAY_OF_DEFENDER_23_FEB
        }
        if (now.month == Month.APRIL && listOf(20, 21, 22).contains(now.dayOfMonth)) {
            return PhraseTheme.ACAB
        }
        return null
    }
}

class DictionaryContext(
    private val settingsKey: ChatSettingsKey,
    private val callback: (Phrase, ChatSettingsKey) -> String
) {
    fun get(phrase: Phrase) = callback(phrase, settingsKey)
}
