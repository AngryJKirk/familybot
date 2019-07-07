package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.repos.ifaces.QuoteRepository
import space.yaroslav.familybot.route.models.Command

@Component
class QuoteExecutor(val quoteRepository: QuoteRepository) : CommandExecutor {
    override fun command(): Command {
        return Command.QUOTE
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        return { it.send(update, quoteRepository.getRandom()) }
    }
}
