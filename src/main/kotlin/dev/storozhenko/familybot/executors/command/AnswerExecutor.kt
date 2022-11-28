package dev.storozhenko.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import dev.storozhenko.familybot.common.extensions.capitalized
import dev.storozhenko.familybot.common.extensions.dropLastDelimiter
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.getLogger
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.telegram.Command
import java.util.regex.Pattern

@Component
class AnswerExecutor : CommandExecutor() {
    private val log = getLogger()
    private val orPattern = Pattern.compile(" (или|або) ")
    override fun command(): Command {
        return Command.ANSWER
    }

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val text = context.message.text

        val message = text
            .removeRange(0, getIndexOfQuestionStart(text) + 1)
            .split(orPattern)
            .filter(String::isNotEmpty)
            .takeIf(this::isOptionsCountEnough)
            ?.random()
            ?.capitalized()
            ?.dropLastDelimiter()
            ?: return {
                log.info("Bad argument was passed, text of message is [{}]", text)
                it.send(context, context.phrase(Phrase.BAD_COMMAND_USAGE), replyToUpdate = true)
            }
        return { it.send(context, message, replyToUpdate = true, shouldTypeBeforeSend = true) }
    }

    private fun isOptionsCountEnough(options: List<String>) = options.size >= 2

    private fun getIndexOfQuestionStart(text: String) = text.indexOfFirst { it == ' ' }.takeIf { it >= 0 } ?: 0
}
