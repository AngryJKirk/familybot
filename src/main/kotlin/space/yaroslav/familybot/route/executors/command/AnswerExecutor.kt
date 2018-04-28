package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.utils.dropLastDelimiter
import space.yaroslav.familybot.common.utils.random
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.route.models.Command

@Component
class AnswerExecutor : CommandExecutor {
    override fun command(): Command {
        return Command.ANSWER
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val text = update.message.text
        val message = text
            .removeRange(0, text.indexOfFirst { it == ' ' }.takeIf { it >= 0 } ?: 0)
            .split(" или ")
            .filter { variant -> variant.isNotEmpty() }
            .takeIf { it.size >= 2 }
            ?.random()
            ?.capitalize()
            ?.dropLastDelimiter()
            ?: "Ты пидор, отъебись, читай как надо использовать команду"
        return {
            it.execute(
                SendMessage(update.toChat().id, message)
                    .setReplyToMessageId(update.message.messageId)
            )
        }
    }
}

