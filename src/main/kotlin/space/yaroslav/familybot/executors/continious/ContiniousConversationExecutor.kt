package space.yaroslav.familybot.executors.continious

import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.Priority
import space.yaroslav.familybot.telegram.BotConfig

abstract class ContiniousConversationExecutor(private val config: BotConfig) : CommandExecutor() {

    override fun priority(executorContext: ExecutorContext): Priority {
        return Priority.MEDIUM
    }

    override fun canExecute(executorContext: ExecutorContext): Boolean {
        val message = executorContext.message
        return message.from.userName == config.botName &&
            (message.text ?: "") == getDialogMessage(executorContext)
    }

    abstract fun getDialogMessage(executorContext: ExecutorContext): String
}
