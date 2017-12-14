package space.yaroslav.familybot.route

import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender


interface Executor {

    fun execute(update: Update): (AbsSender) -> Unit

    fun canExecute(message: Message): Boolean


}