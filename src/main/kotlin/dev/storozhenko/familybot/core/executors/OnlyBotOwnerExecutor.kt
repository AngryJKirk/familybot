package dev.storozhenko.familybot.core.executors

import dev.storozhenko.familybot.common.TrackingAbsSender
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority
import dev.storozhenko.familybot.getLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.bots.AbsSender
import kotlin.time.Duration.Companion.minutes

abstract class OnlyBotOwnerExecutor : PrivateMessageExecutor {
    companion object {
        private val deleteMessageScope = CoroutineScope(Dispatchers.Default)
    }

    override fun canExecute(context: ExecutorContext): Boolean {
        val message = context.message
        return context.isFromDeveloper && message.text?.startsWith(getMessagePrefix(), ignoreCase = true) ?: false
    }

    override fun priority(context: ExecutorContext) = Priority.HIGH

    abstract fun getMessagePrefix(): String

    abstract fun executeInternal(context: ExecutorContext): suspend (AbsSender) -> Unit

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        if (context.testEnvironment) return executeInternal(context)
        return { sender ->
            val trackingAbsSender = TrackingAbsSender(sender)
            executeInternal(context).invoke(trackingAbsSender)
            val idsToDelete = trackingAbsSender.tracking
                .map { DeleteMessage(it.chat.id.toString(), it.messageId) }
                .plus(DeleteMessage(context.chat.idString, context.message.messageId))

            deleteMessageScope.launch {
                runCatching {
                    delay(3.minutes)
                    idsToDelete.forEach(sender::execute)
                }.onFailure { e -> getLogger().error("Failed to delete message", e) }
            }
        }
    }
}
