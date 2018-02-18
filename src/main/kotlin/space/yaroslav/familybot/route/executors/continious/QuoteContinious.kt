package space.yaroslav.familybot.route.executors.continious

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.repos.ifaces.QuoteRepository
import space.yaroslav.familybot.route.executors.command.QUOTE_MESSAGE
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.telegram.BotConfig

@Component
class QuoteContinious(val quoteRepository: QuoteRepository,
                      override val botConfig: BotConfig) : ContiniousConversation {

    override fun command(): Command {
        return Command.QUOTE_BY_TAG
    }

    override fun getDialogMessage(): String {
        return QUOTE_MESSAGE
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        return {
            it.execute((SendMessage(update.message.chatId,
                    quoteRepository.getByTag(update.message.text) ?: "Такого тега нет, идите нахуй"))
                    .setReplyMarkup(ReplyKeyboardRemove()))

        }
    }
}