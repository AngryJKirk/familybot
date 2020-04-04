package space.yaroslav.familybot.executors.continious

import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.models.Priority
import space.yaroslav.familybot.telegram.BotConfig

abstract class ContiniousConversation(private val config: BotConfig) : CommandExecutor(config) {

    override fun priority(update: Update): Priority {
        return Priority.MEDIUM
    }

    override fun canExecute(message: Message): Boolean {
        return message.from.userName == config.botname &&
            message.text ?: "" == getDialogMessage()
    }

    abstract fun getDialogMessage(): String
}
