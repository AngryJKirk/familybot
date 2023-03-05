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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.bots.AbsSender
import kotlin.time.Duration.Companion.minutes

@Component
class GPTStatsExecutor(
    private val commonRepository: CommonRepository,
    private val easyKeyValueService: EasyKeyValueService,
    botConfig: BotConfig
) : OnlyBotOwnerExecutor(botConfig) {
    override fun getMessagePrefix() = "gpt"

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val chats = commonRepository.getChatsAll().associateBy { it.id }
        val stats = easyKeyValueService.getAllByPartKey(ChatGPTTokenUsageByChat)
        val message = stats
            .map { (chat, value) -> formatChat(chats[chat.chatId]) to value }
            .sortedByDescending { (_, value) -> value }
            .joinToString(separator = "\n") { (chat, value) ->
                "${formatValue(value)} ⬅️   $chat"
            }
        val total = formatValue(stats.values.sum())
        return {
            val message1 = it.send(context, message, enableHtml = true)
            val message2 = it.send(context, "Всего потрачено: $total", enableHtml = true)
            coroutineScope {
                launch {
                    delay(1.minutes)
                    it.execute(DeleteMessage(context.chat.idString, context.message.messageId))
                    it.execute(DeleteMessage(message1.chat.id.toString(), message1.messageId))
                    it.execute(DeleteMessage(message2.chat.id.toString(), message2.messageId))
                }
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
}