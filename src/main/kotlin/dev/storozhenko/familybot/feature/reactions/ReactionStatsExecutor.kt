package dev.storozhenko.familybot.feature.reactions


import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.reactions.ReactionsPeriod.Companion.AI_PREFIX
import org.springframework.stereotype.Component

@Component
class ReactionStatsExecutor : CommandExecutor() {
    override fun command() = Command.REACTION_STATS

    override suspend fun execute(context: ExecutorContext) {
        context.send("За какой период реакции?") {
            keyboard {
                row(ReactionsPeriod.entries.map { period -> button(period.periodName) { period.periodName } })
                row(ReactionsPeriod.entries.map { period -> button(AI_PREFIX + period.periodName) { AI_PREFIX + period.periodName } })
            }
        }
    }



}