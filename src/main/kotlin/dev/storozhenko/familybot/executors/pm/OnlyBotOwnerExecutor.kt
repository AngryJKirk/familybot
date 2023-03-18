package dev.storozhenko.familybot.executors.pm

import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.router.Priority
import dev.storozhenko.familybot.telegram.BotConfig
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.bots.AbsSender
import kotlin.time.Duration.Companion.minutes

abstract class OnlyBotOwnerExecutor(private val botConfig: BotConfig) : PrivateMessageExecutor {

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
            val idsToDelete = trackingAbsSender.tracking.apply { add(context.message) }
            coroutineScope {
                launch {
                    if (context.testEnvironment.not()) {
                        delay(3.minutes)
                        idsToDelete.forEach { message ->
                            sender.execute(DeleteMessage(context.chat.idString, message.messageId))
                        }
                    }
                }
            }
        }
    }
}
