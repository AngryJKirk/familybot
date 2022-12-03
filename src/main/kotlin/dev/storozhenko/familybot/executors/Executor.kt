package dev.storozhenko.familybot.executors

import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.router.Priority
import org.telegram.telegrambots.meta.bots.AbsSender

interface Executor {

    fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit

    fun canExecute(context: ExecutorContext): Boolean

    fun priority(context: ExecutorContext): Priority
}
