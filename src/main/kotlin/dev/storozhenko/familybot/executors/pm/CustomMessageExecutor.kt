package dev.storozhenko.familybot.executors.pm

import dev.storozhenko.familybot.common.extensions.getMessageTokens
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.repos.CommonRepository
import dev.storozhenko.familybot.telegram.BotConfig
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class CustomMessageExecutor(
    private val commonRepository: CommonRepository,
    botConfig: BotConfig
) :
    OnlyBotOwnerExecutor(botConfig) {
    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val tokens = context.update.getMessageTokens(delimiter = "|")

        val chats = commonRepository
            .getChats()
            .filter { chat -> chat.name?.contains(tokens[1], ignoreCase = true) ?: false }
        if (chats.size != 1) {
            return { sender ->
                sender.send(context, "Chat is not found, specify search: $chats")
            }
        }
        return { sender ->
            sender.execute(SendMessage(chats.first().idString, tokens[2]))
            sender.send(context, "Message \"${tokens[2]}\" has been sent")
        }
    }

    override fun getMessagePrefix() = "custom_message|"
}
