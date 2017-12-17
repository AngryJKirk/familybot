package space.yaroslav.familybot.route

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.repos.QuoteRepository

@Component
class QuoteExecutor(val quoteRepository: QuoteRepository) : CommandExecutor() {
    override fun command(): Command {
        return Command.QUOTE
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val split = update.message.text.split(" ")
        return {
            it.execute(
                    if (split.size > 1) {
                        (SendMessage(update.message.chatId, quoteRepository.getByTag(split[1]) ?: quoteRepository.getRandom()))
                    } else {
                        SendMessage(update.message.chatId, quoteRepository.getRandom())
                    })
        }
    }

}