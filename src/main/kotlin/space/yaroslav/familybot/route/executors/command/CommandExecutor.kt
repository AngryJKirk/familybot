package space.yaroslav.familybot.route.executors.command

import org.telegram.telegrambots.api.objects.Message
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.Priority

abstract class CommandExecutor : Executor {
    override fun canExecute(message: Message): Boolean {
        return message.text?.contains(command().command) ?: false
    }

    override fun priority(): Priority {
        return Priority.MEDIUM
    }

    abstract fun command(): Command
}