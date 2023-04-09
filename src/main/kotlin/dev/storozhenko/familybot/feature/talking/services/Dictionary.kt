package dev.storozhenko.familybot.feature.talking.services

import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.keyvalue.models.ChatEasyKey
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.dictionary.PhraseTheme
import dev.storozhenko.familybot.feature.settings.models.UkrainianLanguage
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.Month

@Component
class Dictionary(
    private val keyValueService: EasyKeyValueService,
    private val dictionaryReader: DictionaryReader
) {
    fun getAll(phrase: Phrase): Set<String> {
        return dictionaryReader.getAllPhrases(phrase).toSet()
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
            month == Month.SEPTEMBER || month == Month.OCTOBER -> PhraseTheme.ARMY
            else -> null
        }
    }
}
