package dev.storozhenko.familybot.core.executors

import dev.storozhenko.familybot.common.TrackingAbsSender
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import kotlin.time.Duration.Companion.minutes

abstract class OnlyBotOwnerExecutor : PrivateMessageExecutor {
    companion object {
        private val deleteMessageScope = CoroutineScope(Dispatchers.Default)
    }

    private val log = KotlinLogging.logger {}

    override fun canExecute(context: ExecutorContext): Boolean {
        val message = context.message
        return context.isFromDeveloper && message.text?.startsWith(getMessagePrefix(), ignoreCase = true) ?: false
    }

    override fun priority(context: ExecutorContext) = Priority.HIGH

    abstract fun getMessagePrefix(): String

    abstract suspend fun executeInternal(context: ExecutorContext)

    override suspend fun execute(context: ExecutorContext) {
        if (context.testEnvironment) return executeInternal(context)

        val trackingAbsSender = TrackingAbsSender(context.sender)
        executeInternal(context.copy(sender = trackingAbsSender))
        val idsToDelete = trackingAbsSender.tracking
            .map { DeleteMessage(it.chat.id.toString(), it.messageId) }
            .plus(DeleteMessage(context.chat.idString, context.message.messageId))

        deleteMessageScope.launch {
            runCatching {
                delay(3.minutes)
                idsToDelete.forEach { context.sender.execute(it) }
            }.onFailure { e -> log.error(e) { "Failed to delete message" } }
        }
    }
}
