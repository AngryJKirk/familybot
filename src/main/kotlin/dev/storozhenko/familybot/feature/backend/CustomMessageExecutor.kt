package dev.storozhenko.familybot.feature.backend

import dev.storozhenko.familybot.common.extensions.getMessageTokens
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.OnlyBotOwnerExecutor
import dev.storozhenko.familybot.core.repos.UserRepository
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

@Component
class CustomMessageExecutor(
    private val commonRepository: UserRepository,
) :
    OnlyBotOwnerExecutor() {
    override suspend fun executeInternal(context: ExecutorContext) {
        val tokens = context.update.getMessageTokens(delimiter = "|")

        val chats = commonRepository
            .getChats()
            .filter { chat -> chat.name?.contains(tokens[1], ignoreCase = true) ?: false }
        if (chats.size != 1) {
            context.client.send(context, "Chat is not found, specify search: $chats")
            return
        }

        context.client.execute(SendMessage(chats.first().idString, tokens[2]))
        context.client.send(context, "Message \"${tokens[2]}\" has been sent")
    }

    override fun getMessagePrefix() = "custom_message|"
}
