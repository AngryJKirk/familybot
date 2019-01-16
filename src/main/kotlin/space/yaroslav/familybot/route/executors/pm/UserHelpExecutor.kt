package space.yaroslav.familybot.route.executors.pm

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.route.executors.command.HelpCommandExecutor
import space.yaroslav.familybot.route.models.Priority

@Component
class UserHelpExecutor(val helpExecutor: HelpCommandExecutor) : PrivateMessageExecutor {
    override fun execute(update: Update): (AbsSender) -> Unit {
        return helpExecutor.execute(update)
    }

    override fun canExecute(message: Message) = true

    override fun priority(update: Update): Priority {
        return Priority.VERY_LOW
    }
}
