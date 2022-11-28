package dev.storozhenko.familybot.executors.pm

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.bots.AbsSender
import dev.storozhenko.familybot.common.ErrorLogsDeferredAppender
import dev.storozhenko.familybot.common.extensions.getMessageTokens
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.telegram.BotConfig

@Component
class LogsExecutor(botConfig: BotConfig) : OnlyBotOwnerExecutor(botConfig) {
    override fun getMessagePrefix() = "logs"

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val tokens = context.update.getMessageTokens()
        if (tokens.getOrNull(1) == "clear") {
            ErrorLogsDeferredAppender.errors.clear()
            return { sender -> sender.send(context, "Cleared") }
        }

        return { sender ->
            if (ErrorLogsDeferredAppender.errors.isEmpty()) {
                sender.send(context, "No errors yet")
            } else {
                val errors = ErrorLogsDeferredAppender
                    .errors
                    .joinToString(separator = "\n")
                    .byteInputStream()
                sender.execute(
                    SendDocument(
                        context.chat.idString,
                        InputFile(errors, "error_logs.txt")
                    )
                )
            }
        }
    }
}
