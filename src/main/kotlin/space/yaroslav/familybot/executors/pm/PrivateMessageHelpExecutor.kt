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
        return botConfig.developer != context.message.from.userName
    }

    override fun priority(context: ExecutorContext): Priority {
        return Priority.VERY_LOW
    }
}
