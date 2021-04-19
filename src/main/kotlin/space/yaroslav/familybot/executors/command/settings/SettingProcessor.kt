package space.yaroslav.familybot.executors.command.settings

import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender

interface SettingProcessor {

    fun canProcess(update: Update): Boolean

    fun process(update: Update): suspend (AbsSender) -> Unit
}
