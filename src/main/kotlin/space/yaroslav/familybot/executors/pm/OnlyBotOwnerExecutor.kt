package space.yaroslav.familybot.executors.pm

import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.models.Priority
import space.yaroslav.familybot.telegram.BotConfig

abstract class OnlyBotOwnerExecutor(private val botConfig: BotConfig) : PrivateMessageExecutor {

    override fun canExecute(message: Message): Boolean {
        return botConfig.developer == message.from.userName &&
            message.text.startsWith(getMessagePrefix(), ignoreCase = true)
    }

    override fun priority(update: Update) = Priority.HIGH

    abstract fun getMessagePrefix(): String
}
