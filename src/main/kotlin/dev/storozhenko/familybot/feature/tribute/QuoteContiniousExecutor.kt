package dev.storozhenko.familybot.feature.tribute

import dev.storozhenko.familybot.core.executors.ContiniousConversationExecutor
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.feature.tribute.repos.QuoteRepository
import dev.storozhenko.familybot.telegram.BotConfig
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.bots.AbsSender

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

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        return {
            val callbackQuery = context.update.callbackQuery
            it.execute(AnswerCallbackQuery(callbackQuery.id))
            it.execute(
                (
                        SendMessage(
                            callbackQuery.message.chatId.toString(),
                            quoteRepository.getByTag(callbackQuery.data) ?: "Такого тега нет, идите нахуй"
                        )
                        )
            )
        }
    }
}
