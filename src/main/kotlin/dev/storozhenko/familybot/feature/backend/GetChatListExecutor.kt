package dev.storozhenko.familybot.feature.backend

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.OnlyBotOwnerExecutor
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.repos.UserRepository
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMemberCount
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class GetChatListExecutor(
    private val commonRepository: UserRepository,
) : OnlyBotOwnerExecutor() {

    override fun getMessagePrefix() = "chats"

    override suspend fun executeInternal(context: ExecutorContext) {
        val chats = commonRepository.getChats()

        context.sender.send(context, "Active chats count=${chats.size}")
        val totalUsersCount =
            chats.sumOf { chat -> calculate(context.sender, chat) }
        context.sender.send(context, "Total users count=$totalUsersCount")
    }

    private fun calculate(
        sender: AbsSender,
        chat: Chat,
    ): Int {
        return runCatching { sender.execute(GetChatMemberCount(chat.idString)) }
            .getOrElse { 0 }
    }
}
