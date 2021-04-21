package space.yaroslav.familybot.executors.pm

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMembersCount
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.telegram.BotConfig

@Component
class GetChatListExecutor(
    private val commonRepository: CommonRepository,
    botConfig: BotConfig
) : OnlyBotOwnerExecutor(botConfig) {

    private val prefix = "chats"
    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val chats = commonRepository.getChats()
        return { sender ->
            sender.send(update, "Active chats count=${chats.size}")
            val totalUsersCount = chats.sumOf { chat -> runCatching { sender.execute(GetChatMembersCount()) }.getOrElse { 0 } }
            sender.send(update, "Total users count=$totalUsersCount")
        }
    }

    override fun getMessagePrefix() = prefix
}
