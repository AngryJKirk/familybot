package space.yaroslav.familybot.executors.pm

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.ErrorLogsDeferredAppender
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.telegram.BotConfig

@Component
class LogsExecutor(botConfig: BotConfig) : OnlyBotOwnerExecutor(botConfig) {
    override fun getMessagePrefix() = "logs"

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        return { sender ->
            ErrorLogsDeferredAppender.errors.forEach { errorLog ->
                sender.send(update, errorLog)
            }
        }
    }
}