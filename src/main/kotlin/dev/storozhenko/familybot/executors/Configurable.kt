package dev.storozhenko.familybot.executors

import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.router.FunctionId

interface Configurable {

    fun getFunctionId(context: ExecutorContext): FunctionId
}
