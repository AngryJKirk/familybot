package dev.storozhenko.familybot.feature.tribute.executors

import dev.storozhenko.familybot.common.extensions.capitalized

import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.tribute.repos.QuoteRepository
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow

const val QUOTE_MESSAGE = "Тег?"

@Component
class QuoteByTagExecutor(private val quoteRepository: QuoteRepository) : CommandExecutor() {
    override fun command() = Command.QUOTE_BY_TAG

    override suspend fun execute(context: ExecutorContext) {
        val rows = quoteRepository
            .getTags()
            .map { tag ->
                InlineKeyboardButton(tag.capitalized())
                    .apply { callbackData = tag }
            }
            .chunked(3)
            .map(::InlineKeyboardRow)
        context.send(
            QUOTE_MESSAGE,
            replyToUpdate = true,
            customization = { replyMarkup = InlineKeyboardMarkup(rows) },
        )
    }
}
