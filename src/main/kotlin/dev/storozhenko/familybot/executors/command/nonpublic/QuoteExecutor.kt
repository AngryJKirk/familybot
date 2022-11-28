package dev.storozhenko.familybot.executors.command.nonpublic

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.executors.command.CommandExecutor
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.telegram.Command
import dev.storozhenko.familybot.repos.QuoteRepository

@Component
class QuoteExecutor(private val quoteRepository: QuoteRepository) : CommandExecutor() {
    override fun command(): Command {
        return Command.QUOTE
    }

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        return { it.send(context, quoteRepository.getRandom()) }
    }
}
