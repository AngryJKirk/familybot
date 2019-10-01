package space.yaroslav.familybot.route.executors.command

import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.Priority

interface CommandExecutor : Executor {
    override fun canExecute(message: Message): Boolean {
        val entities = message.entities ?: return false
        return entities.any {
            it.type == "bot_command" &&
                (it.text == command().command ||
                    it.text.startsWith("${command().command}@")) &&
                it.offset == 0
        }
    }

    override fun priority(update: Update): Priority {
        return Priority.MEDIUM
    }

    fun isLoggable(): Boolean {
        return true
    }

    fun command(): Command
}
