package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.dropLastDelimiter
import space.yaroslav.familybot.common.utils.random
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.models.Command
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.services.dictionary.Dictionary
import space.yaroslav.familybot.telegram.BotConfig

@Component
class AnswerExecutor(
    private val dictionary: Dictionary,
    config: BotConfig
) : CommandExecutor(config) {
    override fun command(): Command {
        return Command.ANSWER
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val text = update.message.text
        val message = text
            .removeRange(0, getIndexOfQuestionStart(text))
            .split(" или ")
            .filter(String::isNotEmpty)
            .takeIf(this::isOptionsCountEnough)
            ?.random()
            ?.capitalize()
            ?.dropLastDelimiter()
            ?: return {
                it.send(update, dictionary.get(Phrase.BAD_COMMAND_USAGE), replyToUpdate = true)
            }
        return { it.send(update, message, replyToUpdate = true, shouldTypeBeforeSend = true) }
    }

    private fun isOptionsCountEnough(options: List<String>) = options.size >= 2

    private fun getIndexOfQuestionStart(text: String) = text.indexOfFirst { it == ' ' }.takeIf { it >= 0 } ?: 0
}
