package dev.storozhenko.familybot.core.executors

import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority

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
