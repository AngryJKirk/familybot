package dev.storozhenko.familybot.feature.settings.processors

import dev.storozhenko.familybot.common.extensions.getMessageTokens
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.TikTokDownload
import org.springframework.stereotype.Component

@Component
class TikTokSettingProcessor(
    private val easyKeyValueService: EasyKeyValueService,
) : SettingProcessor {
    override fun canProcess(context: ExecutorContext): Boolean {
        return context.update.getMessageTokens()[1] == "тикток"
    }

    override suspend fun process(context: ExecutorContext) {
        when (context.update.getMessageTokens()[2]) {
            "вкл" -> easyKeyValueService.put(TikTokDownload, context.chatKey, true)
            "выкл" -> easyKeyValueService.put(TikTokDownload, context.chatKey, false)
            else -> {
                context.sender.send(context, context.phrase(Phrase.ADVANCED_SETTINGS_ERROR))
                return
            }
        }
        context.sender.send(context, context.phrase(Phrase.ADVANCED_SETTINGS_OK))
    }
}
