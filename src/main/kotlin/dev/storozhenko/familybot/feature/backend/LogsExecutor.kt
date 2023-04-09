package dev.storozhenko.familybot.feature.backend

import dev.storozhenko.familybot.common.ErrorLogsDeferredAppender
import dev.storozhenko.familybot.common.extensions.getMessageTokens
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.OnlyBotOwnerExecutor
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.objects.InputFile

@Component
class LogsExecutor : OnlyBotOwnerExecutor() {
    override fun getMessagePrefix() = "logs"

    override suspend fun executeInternal(context: ExecutorContext) {
        val tokens = context.update.getMessageTokens()
        if (tokens.getOrNull(1) == "clear") {
            ErrorLogsDeferredAppender.errors.clear()
            context.sender.send(context, "Cleared")
        }

        if (ErrorLogsDeferredAppender.errors.isEmpty()) {
            context.sender.send(context, "No errors yet")
        } else {
            val errors = ErrorLogsDeferredAppender
                .errors
                .joinToString(separator = "\n")
                .byteInputStream()
            context.sender.execute(
                SendDocument(
                    context.chat.idString,
                    InputFile(errors, "error_logs.txt")
                )
            )
        }
    }
}
