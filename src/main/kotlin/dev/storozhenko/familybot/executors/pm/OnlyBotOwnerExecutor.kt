package dev.storozhenko.familybot.executors.pm

import dev.storozhenko.familybot.common.TrackingAbsSender
import dev.storozhenko.familybot.getLogger
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.router.Priority
import dev.storozhenko.familybot.telegram.BotConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.bots.AbsSender
import kotlin.time.Duration.Companion.minutes

abstract class OnlyBotOwnerExecutor(private val botConfig: BotConfig) : PrivateMessageExecutor {
    companion object {
        private val deleteMessageScope = CoroutineScope(Dispatchers.Default)
    }

    override fun canExecute(context: ExecutorContext): Boolean {
        val message = context.message
        return botConfig.developer == message.from.userName &&
            message.text.startsWith(getMessagePrefix(), ignoreCase = true)
    }

    override fun priority(context: ExecutorContext) = Priority.HIGH

    abstract fun getMessagePrefix(): String

    abstract fun executeInternal(context: ExecutorContext): suspend (AbsSender) -> Unit

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        return { sender ->
            val trackingAbsSender = TrackingAbsSender(sender)
            executeInternal(context).invoke(trackingAbsSender)
            val idsToDelete = trackingAbsSender.tracking
                .map { DeleteMessage(it.chat.id.toString(), it.messageId) }
                .plus(DeleteMessage(context.chat.idString, context.message.messageId))

            deleteMessageScope.launch {
                runCatching {
                    if (context.testEnvironment.not()) {
                        delay(3.minutes)
                        idsToDelete.forEach(sender::execute)
                    }
                }.onFailure { e -> getLogger().error("Failed to delete message", e) }
            }
        }
    }
}
