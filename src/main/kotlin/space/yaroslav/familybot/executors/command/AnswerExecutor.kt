package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.capitalized
import space.yaroslav.familybot.common.extensions.dropLastDelimiter
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.telegram.BotConfig
import java.util.regex.Pattern

@Component
class AnswerExecutor(
    private val dictionary: Dictionary,
    config: BotConfig
) : CommandExecutor(config) {
    private val log = getLogger()
    private val orPattern = Pattern.compile(" (или|або) ")
    override fun command(): Command {
        return Command.ANSWER
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val text = update.message.text

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
                it.send(update, dictionary.get(Phrase.BAD_COMMAND_USAGE, update), replyToUpdate = true)
            }
        return { it.send(update, message, replyToUpdate = true, shouldTypeBeforeSend = true) }
    }

    private fun isOptionsCountEnough(options: List<String>) = options.size >= 2

    private fun getIndexOfQuestionStart(text: String) = text.indexOfFirst { it == ' ' }.takeIf { it >= 0 } ?: 0
}
