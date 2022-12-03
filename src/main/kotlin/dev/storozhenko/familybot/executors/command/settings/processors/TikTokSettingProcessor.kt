package dev.storozhenko.familybot.executors.command.settings.processors

import dev.storozhenko.familybot.common.extensions.getMessageTokens
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.services.settings.TikTokDownload
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class TikTokSettingProcessor(
    private val easyKeyValueService: EasyKeyValueService
) : SettingProcessor {
    override fun canProcess(context: ExecutorContext): Boolean {
        return context.update.getMessageTokens()[1] == "тикток"
    }

    override fun process(context: ExecutorContext): suspend (AbsSender) -> Unit {
        when (context.update.getMessageTokens()[2]) {
            "вкл" -> easyKeyValueService.put(TikTokDownload, context.chatKey, true)
            "выкл" -> easyKeyValueService.put(TikTokDownload, context.chatKey, false)
            else -> return { it.send(context, context.phrase(Phrase.ADVANCED_SETTINGS_ERROR)) }
        }
        return { it.send(context, context.phrase(Phrase.ADVANCED_SETTINGS_OK)) }
    }
}
