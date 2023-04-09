package dev.storozhenko.familybot.feature.backend

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.OnlyBotOwnerExecutor
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class OwnerPrivateMessageHelpExecutor(
    onlyBotOwnerExecutors: List<OnlyBotOwnerExecutor>
) : OnlyBotOwnerExecutor() {
    override fun getMessagePrefix() = "help"
    private val helpMessage = onlyBotOwnerExecutors
        .map { executor -> executor.getMessagePrefix() to executor::class.java.simpleName }
        .sortedBy { (prefix, _) -> prefix }
        .joinToString("\n") { (prefix, executorName) -> "$prefix — $executorName" }

    override fun executeInternal(context: ExecutorContext): suspend (AbsSender) -> Unit {
        return { sender -> sender.send(context, helpMessage) }
    }
}
