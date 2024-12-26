package dev.storozhenko.familybot.feature.settings.processors

import dev.storozhenko.familybot.common.extensions.getMessageTokens

import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.ChatGPTTalkingDisabled
import org.springframework.stereotype.Component

@Component
class AiOverrideSettingProcessor(
    private val easyKeyValueService: EasyKeyValueService
) : SettingProcessor {
    override fun canProcess(context: ExecutorContext): Boolean {
        return context.update.getMessageTokens()[1] == "тупой"
    }

    override suspend fun process(context: ExecutorContext) {
        val state = context.update.getMessageTokens()[2]
        when (state) {
            "вкл" -> easyKeyValueService.put(ChatGPTTalkingDisabled, context.chatKey, true)
            "выкл" -> easyKeyValueService.put(ChatGPTTalkingDisabled, context.chatKey, false)
            else -> {
                context.send("надо передать вкл или выкл, попробуй еще раз")
                return
            }
        }
        context.send("ок, $state")
    }
}