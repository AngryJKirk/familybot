package space.yaroslav.familybot.route

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import space.yaroslav.familybot.repos.QuoteRepository

@Component
class QuoteExecutor(val quoteRepository: QuoteRepository) : Executor {
    override fun execute(update: Update): SendMessage? {
        val split = update.message.text.split(" ")
        return if (split.size > 1){
            SendMessage(update.message.chatId, quoteRepository.getByTag(split[1])?:quoteRepository.getRandom())
        }
        else{
            SendMessage(update.message.chatId, quoteRepository.getRandom())
        }
    }

    override fun canExecute(update: Update): Boolean {
       return update.message.text.contains("/quote")
    }
}