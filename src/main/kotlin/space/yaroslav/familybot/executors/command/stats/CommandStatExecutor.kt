package space.yaroslav.familybot.executors.command.stats

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.bold
import space.yaroslav.familybot.common.extensions.formatTopList
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.models.telegram.CommandByUser
import space.yaroslav.familybot.repos.CommandHistoryRepository

@Component
class CommandStatExecutor(
    private val repositoryCommand: CommandHistoryRepository
) : CommandExecutor() {

    override fun command(): Command {
        return Command.COMMAND_STATS
    }

    override fun execute(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {

        val all = repositoryCommand.getAll(executorContext.chat).groupBy(CommandByUser::command)

        val topList = all
            .filterNot { it.key == command() }
            .map { format(it, executorContext) }
            .joinToString("\n")

        return {
            it.send(
                executorContext,
                "${executorContext.phrase(Phrase.STATS_BY_COMMAND)}:\n".bold() + topList,
                enableHtml = true
            )
        }
    }

    private fun format(it: Map.Entry<Command, List<CommandByUser>>, executorContext: ExecutorContext) =
        "${executorContext.phrase(Phrase.COMMAND)} " + "${it.key.command}:".bold() + "\n" + it.value.map { it.user }
            .formatTopList()
            .joinToString("\n")
}
