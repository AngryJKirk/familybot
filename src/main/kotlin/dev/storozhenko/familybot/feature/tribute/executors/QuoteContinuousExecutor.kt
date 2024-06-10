package dev.storozhenko.familybot.feature.tribute.executors

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.core.executors.ContinuousConversationExecutor
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.tribute.repos.QuoteRepository
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

@Component
class QuoteContinuousExecutor(
    private val quoteRepository: QuoteRepository,
    botConfig: BotConfig,
) : ContinuousConversationExecutor(botConfig) {

    override fun command() = Command.QUOTE_BY_TAG

    override fun getDialogMessages(context: ExecutorContext) = setOf(QUOTE_MESSAGE)

    override suspend fun execute(context: ExecutorContext) {
        val callbackQuery = context.update.callbackQuery
        context.client.execute(AnswerCallbackQuery(callbackQuery.id))
        context.client.execute(
            (
                    SendMessage(
                        callbackQuery.message.chatId.toString(),
                        quoteRepository.getByTag(callbackQuery.data) ?: "Такого тега нет, идите нахуй",
                    )
                    ),
        )
    }
}
