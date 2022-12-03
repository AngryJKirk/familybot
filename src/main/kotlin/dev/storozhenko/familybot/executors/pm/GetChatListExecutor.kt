package dev.storozhenko.familybot.executors.pm

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.telegram.Chat
import dev.storozhenko.familybot.repos.CommonRepository
import dev.storozhenko.familybot.telegram.BotConfig
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMemberCount
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class GetChatListExecutor(
    private val commonRepository: CommonRepository,
    botConfig: BotConfig
) : OnlyBotOwnerExecutor(botConfig) {

    override fun getMessagePrefix() = "chats"

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val chats = commonRepository.getChats()
        return { sender ->
            sender.send(context, "Active chats count=${chats.size}")
            val totalUsersCount =
                chats.sumOf { chat -> calculate(sender, chat) }
            sender.send(context, "Total users count=$totalUsersCount")
        }
    }

    private fun calculate(
        sender: AbsSender,
        chat: Chat
    ): Int {
        return runCatching { sender.execute(GetChatMemberCount(chat.idString)) }
            .getOrElse { 0 }
    }
}
