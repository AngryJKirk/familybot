package dev.storozhenko.familybot.feature.stats

import dev.storozhenko.familybot.common.extensions.DateConstants
import dev.storozhenko.familybot.common.extensions.bold
import dev.storozhenko.familybot.common.extensions.pluralize
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.logging.repos.CommandHistoryRepository
import org.springframework.stereotype.Component

@Component
class CommandStatExecutor(
    private val repositoryCommand: CommandHistoryRepository,
) : CommandExecutor() {

    override fun command() = Command.COMMAND_STATS

    override suspend fun execute(context: ExecutorContext) {
        val topList = repositoryCommand.get(context.user, from = DateConstants.theBirthDayOfFamilyBot)
            .asSequence()
            .filter { it.command != command() }
            .groupBy { it.command }
            .toList()
            .sortedByDescending { (_, list) -> list.size }
            .joinToString(separator = "\n") { (command, list) -> "${command.command}: ${list.size} ${pluralize(list.size)}" }

        context.client.send(
            context,
            "${context.phrase(Phrase.STATS_BY_COMMAND)}:\n".bold() + topList,
            enableHtml = true,
        )
    }
}
