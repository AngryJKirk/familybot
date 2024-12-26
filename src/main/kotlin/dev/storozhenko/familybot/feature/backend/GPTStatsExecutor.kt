package dev.storozhenko.familybot.feature.backend

import dev.storozhenko.familybot.common.extensions.bold
import dev.storozhenko.familybot.common.extensions.code
import dev.storozhenko.familybot.common.extensions.prettyFormat

import dev.storozhenko.familybot.core.executors.OnlyBotOwnerExecutor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.repos.UserRepository
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.ChatGPTPaidTill
import dev.storozhenko.familybot.feature.settings.models.ChatGPTTokenUsageByChat
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class GPTStatsExecutor(
    private val commonRepository: UserRepository,
    private val easyKeyValueService: EasyKeyValueService,
) : OnlyBotOwnerExecutor() {
    override fun getMessagePrefix() = "gpt"

    override suspend fun executeInternal(context: ExecutorContext) {
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

        context.send(message, enableHtml = true)
        context.send("Всего потрачено: $total", enableHtml = true)
        context.send(subs, enableHtml = true)
    }

    private fun activeSubs(chats: Map<Long, Chat>): String {
        val allSubs = easyKeyValueService.getAllByPartKey(ChatGPTPaidTill)
            .toList()
        val activeSubs = allSubs
            .filter { (_, time) -> time > Instant.now() }
        val inactiveSubs = allSubs
            .filter { (_, time) -> time <= Instant.now() }
        val activeSubsAmount = "Активных подписок: ${activeSubs.size.toString().bold()}"
        val inactiveSubsAmount = "Неактивных подписок: ${inactiveSubs.size.toString().bold()}"
        return listOf(activeSubsAmount, inactiveSubsAmount).plus(
            activeSubs
                .sortedBy { (_, time) -> time }
                .map { (chatKey, time) ->
                    time.prettyFormat(dateOnly = true).code() + "  ⌛️  " +
                            (chats[chatKey.chatId]?.name ?: "#no_name").bold()
                },
        )
            .joinToString(separator = "\n")
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
