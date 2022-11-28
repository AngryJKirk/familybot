package dev.storozhenko.familybot.executors.pm

import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.router.Priority
import dev.storozhenko.familybot.telegram.BotConfig

abstract class OnlyBotOwnerExecutor(private val botConfig: BotConfig) : PrivateMessageExecutor {

    override fun canExecute(context: ExecutorContext): Boolean {
        val message = context.message
        return botConfig.developer == message.from.userName &&
            message.text.startsWith(getMessagePrefix(), ignoreCase = true)
    }

    override fun priority(context: ExecutorContext) = Priority.HIGH

    abstract fun getMessagePrefix(): String
}
