package dev.storozhenko.familybot.feature.tribute.executors

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.core.executors.ContiniousConversationExecutor
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.tribute.repos.QuoteRepository
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

@Component
class QuoteContiniousExecutor(
    private val quoteRepository: QuoteRepository,
    botConfig: BotConfig
) : ContiniousConversationExecutor(botConfig) {

    override fun command(): Command {
        return Command.QUOTE_BY_TAG
    }

    override fun getDialogMessages(context: ExecutorContext): Set<String> {
        return setOf(QUOTE_MESSAGE)
    }

    override suspend fun execute(context: ExecutorContext) {
        val callbackQuery = context.update.callbackQuery
        context.sender.execute(AnswerCallbackQuery(callbackQuery.id))
        context.sender.execute(
            (
                    SendMessage(
                        callbackQuery.message.chatId.toString(),
                        quoteRepository.getByTag(callbackQuery.data) ?: "Такого тега нет, идите нахуй"
                    )
                    )
        )
    }
}
