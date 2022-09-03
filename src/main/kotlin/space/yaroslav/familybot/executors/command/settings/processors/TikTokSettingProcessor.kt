package space.yaroslav.familybot.executors.command.settings.processors

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.getMessageTokens
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.TikTokDownload

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