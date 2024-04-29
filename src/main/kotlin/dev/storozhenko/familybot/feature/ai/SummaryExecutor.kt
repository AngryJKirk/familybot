package dev.storozhenko.familybot.feature.ai

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.logging.repos.RawChatLogRepository
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

    override suspend fun execute(context: ExecutorContext) {
        val paidTill = easyKeyValueService.get(ChatGPTPaidTill, context.chatKey)
        val isCooldown = easyKeyValueService.get(ChatGPTSummaryCooldown, context.chatKey, false)
        if (paidTill == null || paidTill.isBefore(Instant.now())) {
            if (isCooldown) {
                context.sender.send(
                    context,
                    "Саммари на кулдауне, кулдаун 24 часа, если есть подписка из /shop то кулдаун там 5 минут"
                )
                return
            } else {
                easyKeyValueService.put(ChatGPTSummaryCooldown, context.chatKey, true, 24.hours)
            }
        } else {
            if (isCooldown) {
                context.sender.send(context, "Падажжи, кулдаун, всего 5 минут")
                return
            } else {
                easyKeyValueService.put(ChatGPTSummaryCooldown, context.chatKey, true, 5.minutes)
            }
        }
        val prefix = """
            Ниже будет переписка из чата в формате ПОЛЬЗОВАТЕЛЬ >>>> СООБЩЕНИЕ.
            Сделай выжимку из этих сообщений и напиши ее в забавно и информативной форме.
            Главная цель сделать это развлекательным.
        """.trimIndent()
        val messages = rawChatLogRepository
            .getMessages(context.chat)
            .reversed()
            .joinToString(separator = "\n") { (user, message) -> "${user.getGeneralName(false)} >>>> $message" }
        context.sender.send(context, talkingServiceChatGpt.internalMessage(prefix + "\n" + messages))
    }
}