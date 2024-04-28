package dev.storozhenko.familybot.feature.ai

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.logging.repos.RawChatLogRepository
import dev.storozhenko.familybot.feature.talking.services.TalkingServiceChatGpt
import org.springframework.stereotype.Component

@Component
class SummaryExecutor(
    private val talkingServiceChatGpt: TalkingServiceChatGpt,
    private val rawChatLogRepository: RawChatLogRepository
) : CommandExecutor() {
    override fun command() = Command.SUMMARY

    override suspend fun execute(context: ExecutorContext) {
        val prefix = """
            Ниже будет переписка из чата в формате ПОЛЬЗОВАТЕЛЬ >>>> СООБЩЕНИЕ.
            Сделай выжимку из этих сообщений и напиши ее в забавно и информативной форме.
            Главная цель сделать это развлекательным.
        """.trimIndent()
        val messages = rawChatLogRepository
            .getMessages(context.chat)
            .joinToString(separator = "\n") { (user, message) -> "${user.getGeneralName(false)} >>>> $message" }
        context.sender.send(context, talkingServiceChatGpt.internalMessage(prefix + "\n" + messages))
    }
}