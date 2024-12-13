package dev.storozhenko.familybot.feature.ai

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.common.extensions.toJson
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.logging.repos.RawChatLogRepository
import dev.storozhenko.familybot.feature.settings.models.ChatGPT4Enabled
import dev.storozhenko.familybot.feature.settings.models.ChatGPTPaidTill
import dev.storozhenko.familybot.feature.settings.models.ChatGPTSummaryCooldown
import dev.storozhenko.familybot.feature.talking.services.TalkingServiceChatGpt
import org.springframework.stereotype.Component
import java.time.Instant
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Component
class SummaryExecutor(
    private val talkingServiceChatGpt: TalkingServiceChatGpt,
    private val rawChatLogRepository: RawChatLogRepository,
    private val easyKeyValueService: EasyKeyValueService
) : CommandExecutor() {
    override fun command() = Command.SUMMARY

    @Suppress("unused")
    private class UserMessage(
        val messageNumber: Int,
        val username: String,
        val message: String
    )

    override suspend fun execute(context: ExecutorContext) {
        val paidTill = easyKeyValueService.get(ChatGPTPaidTill, context.chatKey)
        val isCooldown = easyKeyValueService.get(ChatGPTSummaryCooldown, context.chatKey, false)
        if (paidTill == null || paidTill.isBefore(Instant.now())) {
            if (isCooldown) {
                context.client.send(
                    context,
                    "Саммари на кулдауне, кулдаун 24 часа, если есть подписка из /shop то кулдаун там 5 минут"
                )
                return
            } else {
                easyKeyValueService.put(ChatGPTSummaryCooldown, context.chatKey, true, 24.hours)
            }
        } else {
            if (isCooldown) {
                context.client.send(context, "Падажжи, кулдаун, всего 5 минут")
                return
            } else {
                easyKeyValueService.put(ChatGPTSummaryCooldown, context.chatKey, true, 5.minutes)
            }
        }
        val prefix = """
            Ниже будет переписка из чата в формате JSON.
            Сделай выжимку из этих сообщений и напиши ее в забавно и информативной форме.
            Главная цель сделать это развлекательным.
            Запрещено использовать html и markdown.
        """.trimIndent()
        val messages = rawChatLogRepository
            .getMessages(context.chat)
            .reversed()
            .mapIndexed { id, (user, message) -> UserMessage(id, user.getGeneralName(false), message) }
            .toJson(pretty = true)
        val useChatGpt4 = easyKeyValueService.get(ChatGPT4Enabled, context.chatKey, false)
        context.client.send(context, talkingServiceChatGpt.internalMessage(prefix + "\n" + messages, useChatGpt4))
    }
}