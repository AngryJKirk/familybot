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

    override fun executeInternal(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val chats = commonRepository.getChatsAll().associateBy { it.id }
        val stats = easyKeyValueService.getAllByPartKey(ChatGPTTokenUsageByChat)
        val message = stats
            .map { (chat, value) -> formatChat(chats[chat.chatId]) to value }
            .sortedByDescending { (_, value) -> value }
            .take(20)
            .joinToString(separator = "\n") { (chat, value) ->
                "${formatValue(value)} ⬅️   $chat"
            }
        val total = formatValue(stats.values.sum())
        return {
            it.send(context, message, enableHtml = true)
            it.send(context, "Всего потрачено: $total", enableHtml = true)
        }
    }
}

private fun formatChat(chat: Chat?): String {
    if (chat == null) {
        return "хуйня какая-то, чата нет"
    }
    return "${chat.name}:${chat.id}".bold()
}

private fun formatValue(value: Long): String {
    return "$value ≈$${String.format("%.3f", value / 1000 * 0.002)}".padEnd(13, ' ').code()
}
