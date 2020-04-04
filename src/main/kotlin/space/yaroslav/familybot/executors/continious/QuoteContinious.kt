package space.yaroslav.familybot.executors.continious

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.executors.command.QUOTE_MESSAGE
import space.yaroslav.familybot.models.Command
import space.yaroslav.familybot.repos.ifaces.QuoteRepository
import space.yaroslav.familybot.telegram.BotConfig

@Component
class QuoteContinious(
    private val quoteRepository: QuoteRepository,
    botConfig: BotConfig
) : ContiniousConversation(botConfig) {

    override fun command(): Command {
        return Command.QUOTE_BY_TAG
    }

    override fun getDialogMessage(): String {
        return QUOTE_MESSAGE
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        return {
            val callbackQuery = update.callbackQuery
            it.execute(AnswerCallbackQuery().setCallbackQueryId(callbackQuery.id))
            it.execute(
                (SendMessage(
                    callbackQuery.message.chatId,
                    quoteRepository.getByTag(callbackQuery.data) ?: "Такого тега нет, идите нахуй"
                ))
            )
        }
    }
}
