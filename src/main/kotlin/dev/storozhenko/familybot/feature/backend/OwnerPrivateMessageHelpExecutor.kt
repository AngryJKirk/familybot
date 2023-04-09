package dev.storozhenko.familybot.feature.backend

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.OnlyBotOwnerExecutor
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import org.springframework.stereotype.Component

@Component
class OwnerPrivateMessageHelpExecutor(
    onlyBotOwnerExecutors: List<OnlyBotOwnerExecutor>
) : OnlyBotOwnerExecutor() {
    override fun getMessagePrefix() = "help"
    private val helpMessage = onlyBotOwnerExecutors
        .map { executor -> executor.getMessagePrefix() to executor::class.java.simpleName }
        .sortedBy { (prefix, _) -> prefix }
        .joinToString("\n") { (prefix, executorName) -> "$prefix — $executorName" }

    override suspend fun executeInternal(context: ExecutorContext) {
        context.sender.send(context, helpMessage)
    }
}
