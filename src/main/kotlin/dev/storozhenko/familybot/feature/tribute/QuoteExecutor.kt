package dev.storozhenko.familybot.feature.tribute

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.feature.tribute.repos.QuoteRepository
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class QuoteExecutor(private val quoteRepository: QuoteRepository) : CommandExecutor() {
    override fun command(): Command {
        return Command.QUOTE
    }

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        return { it.send(context, quoteRepository.getRandom()) }
    }
}
