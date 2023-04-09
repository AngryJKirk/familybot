package dev.storozhenko.familybot.core.executors

import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.FunctionId

interface Configurable {

    fun getFunctionId(context: ExecutorContext): FunctionId
}
