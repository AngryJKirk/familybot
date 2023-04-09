package dev.storozhenko.familybot.core.executors

import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority

interface Executor {

    suspend fun execute(context: ExecutorContext)

    fun canExecute(context: ExecutorContext): Boolean

    fun priority(context: ExecutorContext): Priority
}
