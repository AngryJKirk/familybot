package space.yaroslav.familybot.executors

import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.Priority

interface Executor {

    fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit

    fun canExecute(context: ExecutorContext): Boolean

    fun priority(context: ExecutorContext): Priority
}
