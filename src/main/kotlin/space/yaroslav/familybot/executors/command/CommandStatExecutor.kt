package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.CommandByUser
import space.yaroslav.familybot.common.utils.bold
import space.yaroslav.familybot.common.utils.formatTopList
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.models.Command
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.repos.CommandHistoryRepository
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.services.talking.DictionaryContext
import space.yaroslav.familybot.telegram.BotConfig

@Component
class CommandStatExecutor(
    private val repositoryCommand: CommandHistoryRepository,
    private val dictionary: Dictionary,
    config: BotConfig
) : CommandExecutor(config) {

    override fun command(): Command {
        return Command.COMMAND_STATS
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val context = dictionary.createContext(update)
        val all = repositoryCommand.getAll(update.message.chat.toChat()).groupBy { it.command }

        val topList = all
            .filterNot { it.key == command() }
            .map { format(it, context)}
            .joinToString("\n")

        return {
            it.send(update, "${context.get(Phrase.STATS_BY_COMMAND)}:\n".bold() + topList, enableHtml = true)
        }
    }

    private fun format(it: Map.Entry<Command, List<CommandByUser>>, context: DictionaryContext) =
        "${context.get(Phrase.COMMAND)} " + "${it.key.command}:".bold() + "\n" + it.value.map { it.user }
            .formatTopList()
            .joinToString("\n")
}
