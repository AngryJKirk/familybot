package dev.storozhenko.familybot.feature.backend

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.OnlyBotOwnerExecutor
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.feature.pidor.repos.CommonRepository
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMemberCount
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class GetChatListExecutor(
    private val commonRepository: CommonRepository
) : OnlyBotOwnerExecutor() {

    override fun getMessagePrefix() = "chats"

    override fun executeInternal(context: ExecutorContext): suspend (AbsSender) -> Unit {
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
