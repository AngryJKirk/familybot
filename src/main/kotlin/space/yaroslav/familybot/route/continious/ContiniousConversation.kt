package space.yaroslav.familybot.route.continious

import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender


interface ContiniousConversation {

    fun canProcessContinious(update: Update): Boolean

    fun processContinious(update: Update): (AbsSender) -> Unit

}