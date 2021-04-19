package space.yaroslav.familybot.executors.command.settings

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.getMessageTokens
import space.yaroslav.familybot.common.utils.key
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.services.settings.EasySettingsService
import space.yaroslav.familybot.services.settings.UkrainianLanguage
import space.yaroslav.familybot.services.talking.Dictionary

@Component
class LanguageSettingProcessor(
    private val easySettingsService: EasySettingsService,
    private val dictionary: Dictionary
) : SettingProcessor {

    override fun canProcess(update: Update): Boolean {
        return update.getMessageTokens()[1] == "хохол"
    }

    override fun process(update: Update): suspend (AbsSender) -> Unit {
        val context = dictionary.createContext(update)
        val value = update.getMessageTokens()[2]
        if (value != "вкл" && value != "выкл") {
            return {
                it.send(
                    update,
                    context.get(Phrase.ADVANCED_SETTINGS_FAILED_UKRAINIAN_CHANGE)
                )
            }
        }
        val setting = value == "вкл"
        easySettingsService.put(UkrainianLanguage, update.toChat().key(), setting)
        return {
            it.send(update, context.get(Phrase.ADVANCED_SETTINGS_OK))
        }
    }
}