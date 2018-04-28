package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.repos.ifaces.QuoteRepository
import space.yaroslav.familybot.route.models.Command

const val QUOTE_MESSAGE = "Тег?"

@Component
class QuoteByTagExecutor(val quoteRepository: QuoteRepository) : CommandExecutor {
    override fun command(): Command {
        return Command.QUOTE_BY_TAG
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        return {
            val rows = quoteRepository
                .getTags()
                .map { InlineKeyboardButton().setText(it.capitalize()).setCallbackData(it) }
                .chunked(3)
            it.execute(
                SendMessage(update.toChat().id, QUOTE_MESSAGE)
                    .setReplyToMessageId(update.message.messageId)
                    .setReplyMarkup(
                        InlineKeyboardMarkup()
                            .setKeyboard(rows)
                    )
            )

        }
    }
}