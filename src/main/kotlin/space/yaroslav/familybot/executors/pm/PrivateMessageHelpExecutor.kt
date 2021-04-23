package space.yaroslav.familybot.executors.pm

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.executors.command.HelpCommandExecutor
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.models.Priority
import space.yaroslav.familybot.services.talking.Dictionary

@Component
class PrivateMessageHelpExecutor(
    private val helpExecutor: HelpCommandExecutor,
    private val dictionary: Dictionary
) : PrivateMessageExecutor {
    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        if (helpExecutor.canExecute(update.message)) {
            return helpExecutor.execute(update)
        } else {
            return {
                it.send(
                    update,
                    dictionary.get(Phrase.PRIVATE_MESSAGE_HELP, update),
                    shouldTypeBeforeSend = true
                )
            }
        }
    }

    override fun canExecute(message: Message) = true

    override fun priority(update: Update): Priority {
        return Priority.VERY_LOW
    }
}
