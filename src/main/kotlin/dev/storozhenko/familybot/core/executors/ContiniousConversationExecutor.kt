package dev.storozhenko.familybot.core.executors

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority

abstract class ContiniousConversationExecutor(private val config: BotConfig) : CommandExecutor() {

    override fun priority(context: ExecutorContext) = Priority.MEDIUM

    override fun canExecute(context: ExecutorContext): Boolean {
        val message = context.message
        return message.from.userName == config.botName &&
                (message.text ?: "") in getDialogMessages(context)
    }

    abstract fun getDialogMessages(context: ExecutorContext): Set<String>
}
