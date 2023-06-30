package dev.storozhenko.familybot.feature.help

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.PrivateMessageExecutor
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority
import org.springframework.stereotype.Component

@Component
class PrivateMessageHelpExecutor(
    private val helpExecutor: HelpCommandExecutor,
) : PrivateMessageExecutor {
    override suspend fun execute(context: ExecutorContext) {
        if (helpExecutor.canExecute(context)) {
            helpExecutor.execute(context)
        } else {
            context.sender.send(
                context,
                context.phrase(Phrase.PRIVATE_MESSAGE_HELP),
                shouldTypeBeforeSend = true,
            )
        }
    }

    override fun canExecute(context: ExecutorContext): Boolean {
        return context.isFromDeveloper.not()
    }

    override fun priority(context: ExecutorContext): Priority {
        return Priority.MEDIUM
    }
}
