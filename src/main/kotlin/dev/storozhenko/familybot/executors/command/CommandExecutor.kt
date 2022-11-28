package dev.storozhenko.familybot.executors.command

import dev.storozhenko.familybot.executors.Executor
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.router.Priority
import dev.storozhenko.familybot.models.telegram.Command

abstract class CommandExecutor : Executor {
    override fun canExecute(context: ExecutorContext): Boolean {
        return command() == context.command
    }

    override fun priority(context: ExecutorContext): Priority {
        return Priority.MEDIUM
    }

    open fun isLoggable(): Boolean {
        return true
    }

    abstract fun command(): Command
}
