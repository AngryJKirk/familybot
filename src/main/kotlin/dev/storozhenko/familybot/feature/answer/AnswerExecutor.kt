package dev.storozhenko.familybot.feature.answer

import dev.storozhenko.familybot.common.extensions.capitalized
import dev.storozhenko.familybot.common.extensions.dropLastDelimiter
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.util.regex.Pattern

@Component
class AnswerExecutor : CommandExecutor() {
    private val log = KotlinLogging.logger {  }
    private val orPattern = Pattern.compile(" (или|або) ")
    override fun command(): Command {
        return Command.ANSWER
    }

    override suspend fun execute(context: ExecutorContext) {
        val text = context.message.text

        val message = text
            .removeRange(0, getIndexOfQuestionStart(text) + 1)
            .split(orPattern)
            .filter(String::isNotEmpty)
            .takeIf(this::isOptionsCountEnough)
            ?.random()
            ?.capitalized()
            ?.dropLastDelimiter()

        if (message == null) {
            log.info { "Bad argument was passed, text of message is [$text]" }
            context.sender.send(context, context.phrase(Phrase.BAD_COMMAND_USAGE), replyToUpdate = true)
            return
        }
        context.sender.send(context, message, replyToUpdate = true, shouldTypeBeforeSend = true)
    }

    private fun isOptionsCountEnough(options: List<String>) = options.size >= 2

    private fun getIndexOfQuestionStart(text: String) = text.indexOfFirst { it == ' ' }.takeIf { it >= 0 } ?: 0
}
