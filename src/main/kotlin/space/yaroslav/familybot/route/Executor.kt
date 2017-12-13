package space.yaroslav.familybot.route

import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update


interface Executor {

    fun execute(update: Update): SendMessage?

    fun canExecute(update: Update): Boolean


}