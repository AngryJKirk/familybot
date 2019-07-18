package space.yaroslav.familybot.route.executors

import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.route.models.Priority

interface Executor {

    fun execute(update: Update): suspend (AbsSender) -> Unit

    fun canExecute(message: Message): Boolean

    fun priority(update: Update): Priority
}
