package dev.storozhenko.familybot.feature.talking.services.keyword

import dev.storozhenko.familybot.core.routers.models.ExecutorContext

interface KeyWordProcessor {

    fun isRandom(context: ExecutorContext) = false

    fun canProcess(context: ExecutorContext): Boolean

    suspend fun process(context: ExecutorContext)
}
