package dev.storozhenko.familybot.feature.tribute.executors

import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.tribute.repos.QuoteRepository
import org.springframework.stereotype.Component

const val QUOTE_MESSAGE = "Тег?"

@Component
class QuoteByTagExecutor(private val quoteRepository: QuoteRepository) : CommandExecutor() {
    override fun command() = Command.QUOTE_BY_TAG

    override suspend fun execute(context: ExecutorContext) {
        context.send(
            QUOTE_MESSAGE,
            replyToUpdate = true,
        ) {
            keyboard {
                quoteRepository.getTags()
                    .chunked(3)
                    .forEach {
                        row { it.forEach { button(it) { it } } }
                    }
            }
        }
    }
}
