package space.yaroslav.familybot.executors.pm

import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.Priority
import space.yaroslav.familybot.telegram.BotConfig

abstract class OnlyBotOwnerExecutor(private val botConfig: BotConfig) : PrivateMessageExecutor {

    override fun canExecute(executorContext: ExecutorContext): Boolean {
        val message = executorContext.message
        return botConfig.developer == message.from.userName &&
            message.text.startsWith(getMessagePrefix(), ignoreCase = true)
    }

    override fun priority(executorContext: ExecutorContext) = Priority.HIGH

    abstract fun getMessagePrefix(): String
}
