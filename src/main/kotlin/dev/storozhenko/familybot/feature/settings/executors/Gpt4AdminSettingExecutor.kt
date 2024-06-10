package dev.storozhenko.familybot.feature.settings.executors

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.common.extensions.toEmoji
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.ChatGPT4Enabled
import org.springframework.stereotype.Component

@Component
class Gpt4AdminSettingExecutor(private val easyKeyValueService: EasyKeyValueService) : CommandExecutor() {
    override fun command() = Command.ENABLE_GPT4

    override suspend fun execute(context: ExecutorContext) {
        val currentValue = easyKeyValueService.get(ChatGPT4Enabled, context.chatKey, false)
        if (context.isFromDeveloper) {
            // haha nice placebo
            easyKeyValueService.put(ChatGPT4Enabled, context.chatKey, currentValue.not())
        }
        context.client.send(
            context,
            "OK, ${currentValue.toEmoji()} => ${currentValue.not().toEmoji()}",
            replyToUpdate = true
        )
    }
}