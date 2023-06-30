package dev.storozhenko.familybot.feature.settings.processors

import dev.storozhenko.familybot.common.extensions.getMessageTokens
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.UkrainianLanguage
import org.springframework.stereotype.Component

@Component
class LanguageSettingProcessor(
    private val easyKeyValueService: EasyKeyValueService,
) : SettingProcessor {

    override fun canProcess(context: ExecutorContext): Boolean {
        return context.update.getMessageTokens()[1] == "хохол"
    }

    override suspend fun process(context: ExecutorContext) {
        val value = context.update.getMessageTokens()[2]
        if (value != "вкл" && value != "выкл") {
            context.sender.send(
                context,
                context.phrase(Phrase.ADVANCED_SETTINGS_FAILED_UKRAINIAN_CHANGE),
            )
            return
        }
        val setting = value == "вкл"
        easyKeyValueService.put(UkrainianLanguage, context.chatKey, setting)
        context.sender.send(context, context.phrase(Phrase.ADVANCED_SETTINGS_OK))
    }
}
