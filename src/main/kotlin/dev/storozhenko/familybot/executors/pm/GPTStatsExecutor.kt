package dev.storozhenko.familybot.executors.pm

import dev.storozhenko.familybot.common.extensions.bold
import dev.storozhenko.familybot.common.extensions.code
import dev.storozhenko.familybot.common.extensions.prettyFormat
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.telegram.Chat
import dev.storozhenko.familybot.repos.CommonRepository
import dev.storozhenko.familybot.services.settings.ChatGPTPaidTill
import dev.storozhenko.familybot.services.settings.ChatGPTTokenUsageByChat
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.telegram.BotConfig
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import java.time.Instant

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
        val subs = activeSubs(chats)
        return {
            it.send(context, message, enableHtml = true)
            it.send(context, "Всего потрачено: $total", enableHtml = true)
            it.send(context, "Активные подписки:\n$subs", enableHtml = true)
        }
    }

    private fun activeSubs(chats: Map<Long, Chat>): String {
        return easyKeyValueService.getAllByPartKey(ChatGPTPaidTill)
            .toList()
            .filter { (_, timestamp) -> timestamp > Instant.now().epochSecond }
            .sortedBy { (_, timestamp) -> timestamp }
            .map { (chatKey, timestamp) -> chats[chatKey.chatId] to Instant.ofEpochSecond(timestamp) }
            .joinToString(separator = "\n") { (chat, date) -> date.prettyFormat(dateOnly = true).code() + "  ⌛️  " +  (chat?.name ?: "#no_name").bold() }
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
}


