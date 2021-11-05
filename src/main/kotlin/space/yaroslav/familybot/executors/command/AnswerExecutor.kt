package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.capitalized
import space.yaroslav.familybot.common.extensions.dropLastDelimiter
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.Command
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
            .removeRange(0, getIndexOfQuestionStart(text))
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
