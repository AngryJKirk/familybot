package dev.storozhenko.familybot.feature.ai

import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.logging.repos.RawChatLogRepository
import dev.storozhenko.familybot.feature.talking.services.GptStyle
import dev.storozhenko.familybot.feature.talking.services.TalkingServiceChatGpt
import org.springframework.stereotype.Component

@Component
class SummaryExecutor(
    private val talkingServiceChatGpt: TalkingServiceChatGpt,
    private val rawChatLogRepository: RawChatLogRepository
) : CommandExecutor() {
    override fun command() = Command.SUMMARY

    override suspend fun execute(context: ExecutorContext) {
        val messages = rawChatLogRepository
            .getMessages(context.chat)
            .joinToString(separator = "\n") { (user, message) -> "${user.getGeneralName(false)} >>>> $message" }
        talkingServiceChatGpt.internalMessage(messages, GptStyle.SUMMARY_ASSISTANT)
    }
}