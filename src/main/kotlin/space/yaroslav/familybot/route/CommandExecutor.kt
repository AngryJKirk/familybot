package space.yaroslav.familybot.route

import org.telegram.telegrambots.api.objects.Message

abstract class CommandExecutor : Executor {
    override fun canExecute(message: Message): Boolean {
        return message.text?.contains(command().command) ?: false
    }

    abstract fun command(): Command
}