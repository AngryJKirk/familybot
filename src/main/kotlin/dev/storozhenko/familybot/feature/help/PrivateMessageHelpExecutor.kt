package dev.storozhenko.familybot.feature.help

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.PrivateMessageExecutor
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class PrivateMessageHelpExecutor(
    private val helpExecutor: HelpCommandExecutor
) : PrivateMessageExecutor {
    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        if (helpExecutor.canExecute(context)) {
            return helpExecutor.execute(context)
        } else {
            return {
                it.send(
                    context,
                    context.phrase(Phrase.PRIVATE_MESSAGE_HELP),
                    shouldTypeBeforeSend = true
                )
            }
        }
    }

    override fun canExecute(context: ExecutorContext): Boolean {
        return context.isFromDeveloper.not()
    }

    override fun priority(context: ExecutorContext): Priority {
        return Priority.MEDIUM
    }
}
