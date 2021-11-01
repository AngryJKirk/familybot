package space.yaroslav.familybot.executors.command.settings.processors

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.getMessageTokens
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.toChat
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.UkrainianLanguage
import space.yaroslav.familybot.services.talking.Dictionary

@Component
class LanguageSettingProcessor(
    private val easyKeyValueService: EasyKeyValueService,
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
        easyKeyValueService.put(UkrainianLanguage, update.toChat().key(), setting)
        return {
            it.send(update, context.get(Phrase.ADVANCED_SETTINGS_OK))
        }
    }
}
