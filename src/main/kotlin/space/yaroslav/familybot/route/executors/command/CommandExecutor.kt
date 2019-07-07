package space.yaroslav.familybot.route.executors.command

import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.Priority

interface CommandExecutor : Executor {
    override fun canExecute(message: Message): Boolean {
        val text = message.text ?: ""
        val command = command().command
        return text.startsWith("$command@") ||
            text == command ||
            text.startsWith("$command ")
    }

    override fun priority(update: Update): Priority {
        return Priority.MEDIUM
    }

    fun isLoggable(): Boolean {
        return true
    }

    fun command(): Command
}
