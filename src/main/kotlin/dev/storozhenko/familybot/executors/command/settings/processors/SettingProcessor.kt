package space.yaroslav.familybot.executors.command.settings.processors

import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.models.router.ExecutorContext

interface SettingProcessor {

    fun canProcess(context: ExecutorContext): Boolean

    fun process(context: ExecutorContext): suspend (AbsSender) -> Unit
}
