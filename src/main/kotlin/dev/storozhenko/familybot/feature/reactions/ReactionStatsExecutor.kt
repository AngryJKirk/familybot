package dev.storozhenko.familybot.feature.reactions


import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import org.springframework.stereotype.Component

@Component
class ReactionStatsExecutor : CommandExecutor() {
    override fun command() = Command.REACTION_STATS

    override suspend fun execute(context: ExecutorContext) {
        context.send("За какой период реакции?", customization = {
            replyMarkup = ReactionsPeriod.toKeyBoard()
        })
    }

}