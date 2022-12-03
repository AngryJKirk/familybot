package dev.storozhenko.familybot.executors.command.settings.processors

import dev.storozhenko.familybot.models.router.ExecutorContext
import org.telegram.telegrambots.meta.bots.AbsSender

interface SettingProcessor {

    fun canProcess(context: ExecutorContext): Boolean

    fun process(context: ExecutorContext): suspend (AbsSender) -> Unit
}
