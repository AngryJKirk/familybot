package space.yaroslav.familybot.executors.pm

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.executors.command.HelpCommandExecutor
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.Priority
import space.yaroslav.familybot.telegram.BotConfig

@Component
class PrivateMessageHelpExecutor(
    private val helpExecutor: HelpCommandExecutor,
    private val botConfig: BotConfig
) : PrivateMessageExecutor {
    override fun execute(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {
        if (helpExecutor.canExecute(executorContext)) {
            return helpExecutor.execute(executorContext)
        } else {
            return {
                it.send(
                    executorContext,
                    executorContext.phrase(Phrase.PRIVATE_MESSAGE_HELP),
                    shouldTypeBeforeSend = true
                )
            }
        }
    }

    override fun canExecute(executorContext: ExecutorContext): Boolean {
        return botConfig.developer != executorContext.message.from.userName
    }

    override fun priority(executorContext: ExecutorContext): Priority {
        return Priority.VERY_LOW
    }
}
