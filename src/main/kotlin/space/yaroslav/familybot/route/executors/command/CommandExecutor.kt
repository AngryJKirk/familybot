package space.yaroslav.familybot.route.executors.command

import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.Priority

abstract class CommandExecutor : Executor {
    override fun canExecute(message: Message): Boolean {
        val text = message.text ?: ""
        val command = command().command
        return text.startsWith(command + "@") ||
                text == command ||
                text.startsWith(command + " ")
    }

    override fun priority(update: Update): Priority {
        return Priority.MEDIUM
    }

    abstract fun command(): Command
}