package dev.storozhenko.familybot.feature.backend

import dev.storozhenko.familybot.common.ErrorLogsDeferredAppender
import dev.storozhenko.familybot.common.extensions.getMessageTokens

import dev.storozhenko.familybot.core.executors.OnlyBotOwnerExecutor
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.talking.services.TalkingServiceChatGpt
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.objects.InputFile
import java.io.InputStream

@Component
class LogsExecutor(private val talkingServiceChatGpt: TalkingServiceChatGpt) : OnlyBotOwnerExecutor() {
    override fun getMessagePrefix() = "logs"

    override suspend fun executeInternal(context: ExecutorContext) {
        val tokens = context.update.getMessageTokens()
        if (tokens.getOrNull(1) == "clear") {
            ErrorLogsDeferredAppender.errors.clear()
            context.send("Cleared")
            return
        }

        if (ErrorLogsDeferredAppender.errors.isEmpty()) {
            context.send("No errors yet")
            return
        }

        val chatGptAnalysis = coroutineScope { async { getChatGptAnalysis() } }
        val errors = ErrorLogsDeferredAppender
            .errors
            .joinToString(separator = "\n")
            .byteInputStream()
        context.client.execute(
            SendDocument(
                context.chat.idString,
                InputFile(errors, "error_logs.txt"),
            )
        )
        context.client.execute(
            SendDocument(
                context.chat.idString,
                InputFile(chatGptAnalysis.await(), "chatgpt_analysis.txt")
            )
        )

    }

    private suspend fun getChatGptAnalysis(): InputStream {
        return talkingServiceChatGpt.internalMessage(
            """
            Summarize the errors/warns from my Java/Kotlin application. Do not give recommendations, I just need a summary.
            The summary should be a list that's sorted by how critical it is in your opinion. Amount of errors does not matter and does not affect how critical it is.
            If you think something is really bad mark it as (!!!).
            Some messages have exceptions, they will be provided with the exception's message and class.
            Here is the log:
           ${ErrorLogsDeferredAppender.messagesToAnalyze.takeLast(20)}  
        """.trimIndent()
        ).byteInputStream()
    }
}
