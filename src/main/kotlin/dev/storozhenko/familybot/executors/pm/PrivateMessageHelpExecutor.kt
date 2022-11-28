package dev.storozhenko.familybot.executors.pm

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.executors.command.HelpCommandExecutor
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.router.Priority

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
