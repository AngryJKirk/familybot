package space.yaroslav.familybot.executors.pm

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMembersCount
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.telegram.BotConfig

@Component
class GetChatListExecutor(
    private val commonRepository: CommonRepository,
    botConfig: BotConfig
) : OnlyBotOwnerExecutor(botConfig) {

    override fun getMessagePrefix() = "chats"

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val chats = commonRepository.getChats()
        return { sender ->
            sender.send(update, "Active chats count=${chats.size}")
            val totalUsersCount =
                chats.sumOf { chat -> calculate(sender, chat) }
            sender.send(update, "Total users count=$totalUsersCount")
        }
    }

    private fun calculate(
        sender: AbsSender,
        chat: Chat
    ): Int {
        return runCatching { sender.execute(GetChatMembersCount(chat.idString)) }
            .getOrElse { 0 }
    }
}
