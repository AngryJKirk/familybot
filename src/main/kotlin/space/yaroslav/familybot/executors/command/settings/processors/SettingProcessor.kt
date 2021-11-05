package space.yaroslav.familybot.executors.command.settings.processors

import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.models.router.ExecutorContext

interface SettingProcessor {

    fun canProcess(executorContext: ExecutorContext): Boolean

    fun process(executorContext: ExecutorContext): suspend (AbsSender) -> Unit
}
