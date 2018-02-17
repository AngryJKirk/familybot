package space.yaroslav.familybot.route.continious

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.repos.ifaces.QuoteRepository
import space.yaroslav.familybot.route.executors.command.QUOTE_MESSAGE

@Component
class QuoteContinious(val quoteRepository: QuoteRepository) : ContiniousConversation {
    override fun canProcessContinious(update: Update): Boolean {
        return update.message?.replyToMessage?.text ?: "" == QUOTE_MESSAGE
    }

    override fun processContinious(update: Update): (AbsSender) -> Unit {
        return {
            it.execute((SendMessage(update.message.chatId,
                    quoteRepository.getByTag(update.message.text) ?: "Такого тега нет, идите нахуй"))
                    .setReplyMarkup(ReplyKeyboardRemove()))

        }
    }
}