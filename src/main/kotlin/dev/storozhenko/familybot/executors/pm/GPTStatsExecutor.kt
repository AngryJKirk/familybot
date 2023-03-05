package dev.storozhenko.familybot.executors.pm

import dev.storozhenko.familybot.common.extensions.bold
import dev.storozhenko.familybot.common.extensions.code
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.telegram.Chat
import dev.storozhenko.familybot.repos.CommonRepository
import dev.storozhenko.familybot.services.settings.ChatGPTTokenUsageByChat
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.telegram.BotConfig
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class GPTStatsExecutor(
    private val commonRepository: CommonRepository,
    private val easyKeyValueService: EasyKeyValueService,
    botConfig: BotConfig
) : OnlyBotOwnerExecutor(botConfig) {
    override fun getMessagePrefix() = "gpt"

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val chats = commonRepository.getChats().associateBy { it.id }
        val message = easyKeyValueService.getAllByPartKey(ChatGPTTokenUsageByChat)
            .map { (chat, value) -> formatChat(chats[chat.chatId]) to value }
            .sortedByDescending { (_, value) -> value }
            .joinToString(separator = "\n") { (chat, value) ->
                "${formatValue(value)} ⬅️   $chat"
            }
        return {
            it.send(context, message, enableHtml = true)
        }
    }

    private fun formatChat(chat: Chat?): String {
        if (chat == null) {
            return "хуйня какая-то, чата нет"
        }
        return "${chat.name}:${chat.id}".bold()
    }

    private fun formatValue(value: Long): String {
        return "$value ≈$${(value / 1000 * 0.002)}".padEnd(13, ' ').code()
    }
}