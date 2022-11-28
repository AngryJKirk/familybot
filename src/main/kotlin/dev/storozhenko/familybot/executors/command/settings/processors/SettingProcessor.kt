package dev.storozhenko.familybot.executors.command.settings.processors

import org.telegram.telegrambots.meta.bots.AbsSender
import dev.storozhenko.familybot.models.router.ExecutorContext

interface SettingProcessor {

    fun canProcess(context: ExecutorContext): Boolean

    fun process(context: ExecutorContext): suspend (AbsSender) -> Unit
}
