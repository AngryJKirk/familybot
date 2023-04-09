package dev.storozhenko.familybot.feature.settings.processors

import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import org.telegram.telegrambots.meta.bots.AbsSender

interface SettingProcessor {

    fun canProcess(context: ExecutorContext): Boolean

    fun process(context: ExecutorContext): suspend (AbsSender) -> Unit
}
