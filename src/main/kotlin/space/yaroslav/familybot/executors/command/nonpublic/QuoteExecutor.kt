package space.yaroslav.familybot.executors.command.nonpublic

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.repos.QuoteRepository
import space.yaroslav.familybot.telegram.BotConfig

@Component
class QuoteExecutor(private val quoteRepository: QuoteRepository, config: BotConfig) : CommandExecutor(config) {
    override fun command(): Command {
        return Command.QUOTE
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        return { it.send(update, quoteRepository.getRandom()) }
    }
}
