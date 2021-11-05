package space.yaroslav.familybot.executors.command.nonpublic

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.capitalized
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.repos.QuoteRepository

const val QUOTE_MESSAGE = "Тег?"

@Component
class QuoteByTagExecutor(private val quoteRepository: QuoteRepository) : CommandExecutor() {
    override fun command(): Command {
        return Command.QUOTE_BY_TAG
    }

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        return {
            val rows = quoteRepository
                .getTags()
                .map { tag -> InlineKeyboardButton(tag.capitalized()).apply { callbackData = tag } }
                .chunked(3)
            it.send(context, QUOTE_MESSAGE, replyToUpdate = true, customization = customization(rows))
        }
    }

    private fun customization(rows: List<List<InlineKeyboardButton>>): SendMessage.() -> Unit {
        return { replyMarkup = InlineKeyboardMarkup(rows) }
    }
}
