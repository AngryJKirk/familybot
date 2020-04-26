package space.yaroslav.familybot.executors.pm

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.executors.command.HelpCommandExecutor
import space.yaroslav.familybot.models.Priority

@Component
class UserHelpExecutor(private val helpExecutor: HelpCommandExecutor) : PrivateMessageExecutor {
    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        if (helpExecutor.canExecute(update.message)) {
            return helpExecutor.execute(update)
        } else {
            return {
                it.send(
                    update,
                    "В группу меня добавь, долбоеб, ну или /help нажми, почитаешь что я умею",
                    shouldTypeBeforeSend = true
                )
            }
        }
    }

    override fun canExecute(message: Message) = true

    override fun priority(update: Update): Priority {
        return Priority.VERY_LOW
    }
}
