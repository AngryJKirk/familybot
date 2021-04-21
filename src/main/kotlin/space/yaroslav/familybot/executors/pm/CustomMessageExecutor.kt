package space.yaroslav.familybot.executors.pm

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.getMessageTokens
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.telegram.BotConfig

@Component
class CustomMessageExecutor(
    private val commonRepository: CommonRepository,
    botConfig: BotConfig
) :
    OnlyBotOwnerExecutor(botConfig) {
    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val tokens = update.getMessageTokens(delimiter = "|")

        val chats = commonRepository
            .getChats()
            .filter { chat -> chat.name?.contains(tokens[1], ignoreCase = true) ?: false }
        if (chats.size != 1) {
            return { sender ->
                sender.send(update, "Chat is not found, specify search: $chats")
            }
        }
        return { sender ->
            sender.execute(SendMessage(chats.first().idString, tokens[2]))
            sender.send(update, "Message \"${tokens[2]}\" has been sent")
        }
    }

    override fun getMessagePrefix() = "custom_message|"
}
