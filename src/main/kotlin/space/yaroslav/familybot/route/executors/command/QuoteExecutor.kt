package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.repos.ifaces.QuoteRepository
import space.yaroslav.familybot.route.models.Command

@Component
class QuoteExecutor(val quoteRepository: QuoteRepository) : CommandExecutor {
    override fun command(): Command {
        return Command.QUOTE
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        return {
            it.execute(SendMessage(update.message.chatId, quoteRepository.getRandom()))
        }
    }
}