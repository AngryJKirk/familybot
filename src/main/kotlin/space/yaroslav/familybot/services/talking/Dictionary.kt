package space.yaroslav.familybot.services.talking

import org.springframework.stereotype.Component
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.dictionary.PhraseTheme
import space.yaroslav.familybot.services.settings.ChatEasyKey
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.UkrainianLanguage
import java.time.LocalDate
import java.time.Month

@Component
class Dictionary(
    private val keyValueService: EasyKeyValueService,
    private val dictionaryReader: DictionaryReader
) {
    fun getAll(phrase: Phrase): List<String> {
        return dictionaryReader.getAllPhrases(phrase)
    }

    fun get(phrase: Phrase, settingsKey: ChatEasyKey) = getInternal(phrase, settingsKey)

    fun getInternal(phrase: Phrase, settingsKey: ChatEasyKey): String {
        val isUkrainian = keyValueService.get(UkrainianLanguage, settingsKey)
        val theme = if (isUkrainian == true) {
            PhraseTheme.UKRAINIAN
        } else {
            getHolidayTheme() ?: PhraseTheme.DEFAULT
        }

        return dictionaryReader.getPhrases(phrase, theme).random()
    }

    private fun getHolidayTheme(): PhraseTheme? {
        val now = LocalDate.now()
        val month = now.month
        val day = now.dayOfMonth
        return when {
            month == Month.JANUARY && day in listOf(22, 23, 24) -> PhraseTheme.ACAB
            month == Month.FEBRUARY && day == 23 -> PhraseTheme.DAY_OF_DEFENDER_23_FEB
            month == Month.MARCH && day == 8 -> PhraseTheme.DAY_OF_WOMAN_8_MARCH
            else -> null
        }
    }
}
