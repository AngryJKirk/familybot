package space.yaroslav.familybot.executors.pm

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMemberCount
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.telegram.BotConfig

@Component
class GetChatListExecutor(
    private val commonRepository: CommonRepository,
    botConfig: BotConfig
) : OnlyBotOwnerExecutor(botConfig) {

    override fun getMessagePrefix() = "chats"

    override fun execute(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {
        val chats = commonRepository.getChats()
        return { sender ->
            sender.send(executorContext, "Active chats count=${chats.size}")
            val totalUsersCount =
                chats.sumOf { chat -> calculate(sender, chat) }
            sender.send(executorContext, "Total users count=$totalUsersCount")
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
