package space.yaroslav.familybot.executors.pm

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.ErrorLogsDeferredAppender
import space.yaroslav.familybot.common.extensions.chatIdString
import space.yaroslav.familybot.common.extensions.getMessageTokens
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.telegram.BotConfig

@Component
class LogsExecutor(botConfig: BotConfig) : OnlyBotOwnerExecutor(botConfig) {
    override fun getMessagePrefix() = "logs"

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val tokens = update.getMessageTokens()
        if (tokens.getOrNull(1) == "clear") {
            ErrorLogsDeferredAppender.errors.clear()
            return { sender -> sender.send(update, "Cleared") }
        }

        return { sender ->
            if (ErrorLogsDeferredAppender.errors.isEmpty()) {
                sender.send(update, "No errors yet")
            } else {
                val errors = ErrorLogsDeferredAppender
                    .errors
                    .joinToString(separator = "\n")
                    .byteInputStream()
                sender.execute(
                    SendDocument(
                        update.chatIdString(),
                        InputFile(errors, "error_logs.txt")
                    )
                )
            }
        }
    }
}
