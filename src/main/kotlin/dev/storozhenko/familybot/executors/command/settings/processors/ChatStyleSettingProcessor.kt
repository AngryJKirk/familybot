package dev.storozhenko.familybot.executors.command.settings.processors

import dev.storozhenko.familybot.common.extensions.getMessageTokens
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.services.settings.ChatGPTStyle
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.services.talking.GptStyle
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class ChatStyleSettingProcessor(
    private val easyKeyValueService: EasyKeyValueService,
) : SettingProcessor {

    override fun canProcess(context: ExecutorContext): Boolean {
        return context.update.getMessageTokens()[1] == "стиль"
    }

    override fun process(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val value = context.update.getMessageTokens()[2]
        val keys = GptStyle.values().map(GptStyle::value)
        if (value in keys) {
            easyKeyValueService.put(ChatGPTStyle, context.chatKey, value)
            return { it.send(context, "ок") }
        } else {
            return { it.send(context, "Нет такого стиля, вот список вариантов: " + keys.joinToString(", ")) }
        }
    }
}