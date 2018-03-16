package space.yaroslav.familybot.route.executors.continious

import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import space.yaroslav.familybot.route.executors.command.CommandExecutor
import space.yaroslav.familybot.route.models.Priority
import space.yaroslav.familybot.telegram.BotConfig


interface ContiniousConversation : CommandExecutor {

    val botConfig: BotConfig

    override fun priority(update: Update): Priority {
        return Priority.MEDIUM
    }

    override fun canExecute(message: Message): Boolean {
        return message.from.userName == botConfig.botname
                && message.text ?: "" == getDialogMessage()
    }

    fun getDialogMessage(): String
}