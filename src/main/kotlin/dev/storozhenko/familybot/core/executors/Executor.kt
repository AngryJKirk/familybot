package dev.storozhenko.familybot.core.executors

import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority
import org.telegram.telegrambots.meta.bots.AbsSender

interface Executor {

    fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit

    fun canExecute(context: ExecutorContext): Boolean

    fun priority(context: ExecutorContext): Priority
}
