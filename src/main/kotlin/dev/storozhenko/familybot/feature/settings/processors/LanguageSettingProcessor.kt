package dev.storozhenko.familybot.feature.settings.processors

import dev.storozhenko.familybot.common.extensions.getMessageTokens
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.feature.settings.models.UkrainianLanguage
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class LanguageSettingProcessor(
    private val easyKeyValueService: EasyKeyValueService
) : SettingProcessor {

    override fun canProcess(context: ExecutorContext): Boolean {
        return context.update.getMessageTokens()[1] == "хохол"
    }

    override fun process(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val value = context.update.getMessageTokens()[2]
        if (value != "вкл" && value != "выкл") {
            return {
                it.send(
                    context,
                    context.phrase(Phrase.ADVANCED_SETTINGS_FAILED_UKRAINIAN_CHANGE)
                )
            }
        }
        val setting = value == "вкл"
        easyKeyValueService.put(UkrainianLanguage, context.chatKey, setting)
        return {
            it.send(context, context.phrase(Phrase.ADVANCED_SETTINGS_OK))
        }
    }
}
