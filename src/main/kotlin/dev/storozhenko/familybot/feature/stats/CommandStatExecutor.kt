package dev.storozhenko.familybot.feature.stats

import dev.storozhenko.familybot.common.extensions.bold
import dev.storozhenko.familybot.common.extensions.formatTopList
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.models.telegram.CommandByUser
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.logging.repos.CommandHistoryRepository
import org.springframework.stereotype.Component

@Component
class CommandStatExecutor(
    private val repositoryCommand: CommandHistoryRepository
) : CommandExecutor() {

    override fun command(): Command {
        return Command.COMMAND_STATS
    }

    override suspend fun execute(context: ExecutorContext) {
        val all = repositoryCommand.getAll(context.chat).groupBy(CommandByUser::command)

        val topList = all
            .filterNot { it.key == command() }
            .map { format(it, context) }
            .joinToString("\n")

        context.sender.send(
            context,
            "${context.phrase(Phrase.STATS_BY_COMMAND)}:\n".bold() + topList,
            enableHtml = true
        )
    }

    private fun format(it: Map.Entry<Command, List<CommandByUser>>, context: ExecutorContext) =
        "${context.phrase(Phrase.COMMAND)} " + "${it.key.command}:".bold() + "\n" + it.value.map { it.user }
            .formatTopList()
            .joinToString("\n")
}
