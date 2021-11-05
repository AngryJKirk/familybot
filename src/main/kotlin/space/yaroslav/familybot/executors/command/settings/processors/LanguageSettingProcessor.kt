package space.yaroslav.familybot.executors.command.settings.processors

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.getMessageTokens
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.toChat
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.UkrainianLanguage

@Component
class LanguageSettingProcessor(
    private val easyKeyValueService: EasyKeyValueService
) : SettingProcessor {

    override fun canProcess(executorContext: ExecutorContext): Boolean {
        return executorContext.update.getMessageTokens()[1] == "хохол"
    }

    override fun process(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {
        
        val value = executorContext.update.getMessageTokens()[2]
        if (value != "вкл" && value != "выкл") {
            return {
                it.send(
                    executorContext,
                    executorContext.phrase(Phrase.ADVANCED_SETTINGS_FAILED_UKRAINIAN_CHANGE)
                )
            }
        }
        val setting = value == "вкл"
        easyKeyValueService.put(UkrainianLanguage, executorContext.update.toChat().key(), setting)
        return {
            it.send(executorContext, executorContext.phrase(Phrase.ADVANCED_SETTINGS_OK))
        }
    }
}
