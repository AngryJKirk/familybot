package dev.storozhenko.familybot.feature.tribute.executors


import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.tribute.repos.QuoteRepository
import org.springframework.stereotype.Component

@Component
class QuoteExecutor(private val quoteRepository: QuoteRepository) : CommandExecutor() {
    override fun command() = Command.QUOTE

    override suspend fun execute(context: ExecutorContext) {
        context.send(quoteRepository.getRandom())
    }
}
