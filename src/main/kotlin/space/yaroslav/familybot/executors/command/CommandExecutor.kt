package space.yaroslav.familybot.executors.command

import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.Priority
import space.yaroslav.familybot.models.telegram.Command

abstract class CommandExecutor : Executor {
    override fun canExecute(executorContext: ExecutorContext): Boolean {
        return command() == executorContext.command
    }

    override fun priority(executorContext: ExecutorContext): Priority {
        return Priority.MEDIUM
    }

    open fun isLoggable(): Boolean {
        return true
    }

    abstract fun command(): Command
}
