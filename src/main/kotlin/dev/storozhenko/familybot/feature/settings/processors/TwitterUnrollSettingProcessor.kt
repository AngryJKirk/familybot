package dev.storozhenko.familybot.feature.settings.processors

import dev.storozhenko.familybot.common.extensions.getMessageTokens
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.TwitterUnroll
import org.springframework.stereotype.Component

@Component
class TwitterUnrollSettingProcessor(
    private val easyKeyValueService: EasyKeyValueService
) : SettingProcessor {

    override fun canProcess(context: ExecutorContext): Boolean {
         return context.update.getMessageTokens()[1] == "твиттер"
    }

    override suspend fun process(context: ExecutorContext) {
         when (context.update.getMessageTokens()[2]) {
            "вкл" -> easyKeyValueService.put(TwitterUnroll, context.chatKey, true)
            "выкл" -> easyKeyValueService.put(TwitterUnroll, context.chatKey, false)
            else -> {
                context.client.send(context, context.phrase(Phrase.ADVANCED_SETTINGS_ERROR))
                return
            }
        }
        context.client.send(context, context.phrase(Phrase.ADVANCED_SETTINGS_OK))
    }
}