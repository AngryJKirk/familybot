package dev.storozhenko.familybot.feature.backend


import dev.storozhenko.familybot.core.executors.OnlyBotOwnerExecutor
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.repos.UserRepository
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMemberCount
import org.telegram.telegrambots.meta.generics.TelegramClient

@Component
class GetChatListExecutor(
    private val commonRepository: UserRepository,
) : OnlyBotOwnerExecutor() {

    override fun getMessagePrefix() = "chats"

    override suspend fun executeInternal(context: ExecutorContext) {
        val chats = commonRepository.getChats()

        context.send("Active chats count=${chats.size}")
        val totalUsersCount =
            chats.sumOf { chat -> calculate(context.client, chat) }
        context.send("Total users count=$totalUsersCount")
    }

    private fun calculate(
        client: TelegramClient,
        chat: Chat,
    ): Int {
        return runCatching { client.execute(GetChatMemberCount(chat.idString)) }
            .getOrElse {
                commonRepository.changeChatActiveStatus(chat, false)
                0
            }
    }
}
