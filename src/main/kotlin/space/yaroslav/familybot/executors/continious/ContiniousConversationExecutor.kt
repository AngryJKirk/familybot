package space.yaroslav.familybot.executors.continious

import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.Priority
import space.yaroslav.familybot.telegram.BotConfig

abstract class ContiniousConversationExecutor(private val config: BotConfig) : CommandExecutor() {

    override fun priority(context: ExecutorContext): Priority {
        return Priority.MEDIUM
    }

    override fun canExecute(context: ExecutorContext): Boolean {
        val message = context.message
        return message.from.userName == config.botName &&
                (message.text ?: "") in getDialogMessages(context)
    }

    abstract fun getDialogMessages(context: ExecutorContext): Set<String>
}
