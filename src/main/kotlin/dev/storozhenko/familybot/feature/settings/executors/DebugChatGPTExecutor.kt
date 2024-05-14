package dev.storozhenko.familybot.feature.settings.executors

import dev.storozhenko.familybot.common.extensions.bold
import dev.storozhenko.familybot.common.extensions.code
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.keyvalue.models.ChatEasyKey
import dev.storozhenko.familybot.core.keyvalue.models.EasyKeyType
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.ChatGPT4Enabled
import dev.storozhenko.familybot.feature.settings.models.ChatGPT4MessagesDailyCounter
import dev.storozhenko.familybot.feature.settings.models.ChatGPTFreeMessagesLeft
import dev.storozhenko.familybot.feature.settings.models.ChatGPTNotificationNeeded
import dev.storozhenko.familybot.feature.settings.models.ChatGPTPaidTill
import dev.storozhenko.familybot.feature.settings.models.ChatGPTReactionsCooldown
import dev.storozhenko.familybot.feature.settings.models.ChatGPTStyle
import dev.storozhenko.familybot.feature.settings.models.ChatGPTSummaryCooldown
import dev.storozhenko.familybot.feature.settings.models.ChatGPTTokenUsageByChat
import org.springframework.stereotype.Component

@Component
class DebugChatGPTExecutor(private val easyKeyValueService: EasyKeyValueService) : CommandExecutor() {
    override fun command() = Command.DEBUG_GPT

    override suspend fun execute(context: ExecutorContext) {

        val message = setOf(
            ChatGPTStyle,
            ChatGPTPaidTill,
            ChatGPTFreeMessagesLeft,
            ChatGPTTokenUsageByChat,
            ChatGPTNotificationNeeded,
            ChatGPTSummaryCooldown,
            ChatGPTReactionsCooldown,
            ChatGPT4Enabled,
            ChatGPT4MessagesDailyCounter,
        ).joinToString(separator = "\n") { key -> getDataByKey(context, key) }

        context.sender.send(context, message, enableHtml = true, replyToUpdate = true)
    }

    private fun getDataByKey(context: ExecutorContext, easyKey: EasyKeyType<out Any, ChatEasyKey>): String {
        val keyValue = easyKeyValueService.get(easyKey, context.chatKey)
        return "${easyKey.getName().bold()} => ${keyValue.toString().code()}"
    }
}