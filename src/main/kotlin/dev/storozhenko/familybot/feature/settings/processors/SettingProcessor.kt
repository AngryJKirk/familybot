package dev.storozhenko.familybot.feature.settings.processors

import dev.storozhenko.familybot.core.routers.models.ExecutorContext

interface SettingProcessor {

    fun canProcess(context: ExecutorContext): Boolean

    suspend fun process(context: ExecutorContext)
}
