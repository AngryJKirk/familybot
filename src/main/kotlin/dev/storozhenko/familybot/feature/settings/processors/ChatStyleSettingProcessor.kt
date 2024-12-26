package dev.storozhenko.familybot.feature.settings.processors

import dev.storozhenko.familybot.common.extensions.getMessageTokens

import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.ChatGPTStyle
import dev.storozhenko.familybot.feature.talking.services.GptStyle
import org.springframework.stereotype.Component

@Component
class ChatStyleSettingProcessor(
    private val easyKeyValueService: EasyKeyValueService,
) : SettingProcessor {

    override fun canProcess(context: ExecutorContext): Boolean {
        val messageTokens = context.update.getMessageTokens()
        return messageTokens.size >= 3 && messageTokens[1] == "стиль"
    }

    override suspend fun process(context: ExecutorContext) {
        val value = context.update.getMessageTokens()[2]
        val keys = GptStyle.entries.map(GptStyle::value)
        if (value in keys) {
            easyKeyValueService.put(ChatGPTStyle, context.chatKey, value)
            context.send("ок")
        } else {
            context.send("Нет такого стиля, вот список вариантов: " + keys.joinToString(", "))
        }
    }
}
