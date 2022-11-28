package dev.storozhenko.familybot.executors.continious

import dev.storozhenko.familybot.executors.command.CommandExecutor
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.router.Priority
import dev.storozhenko.familybot.telegram.BotConfig

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
