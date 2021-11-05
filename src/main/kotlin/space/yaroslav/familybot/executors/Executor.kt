package space.yaroslav.familybot.executors

import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.Priority

interface Executor {

    fun execute(executorContext: ExecutorContext): suspend (AbsSender) -> Unit

    fun canExecute(executorContext: ExecutorContext): Boolean

    fun priority(executorContext: ExecutorContext): Priority
}
