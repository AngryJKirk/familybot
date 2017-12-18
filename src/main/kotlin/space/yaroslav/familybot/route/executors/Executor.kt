package space.yaroslav.familybot.route.executors

import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.route.models.Priority


interface Executor {

    fun execute(update: Update): (AbsSender) -> Unit

    fun canExecute(message: Message): Boolean

    fun priority(): Priority

}