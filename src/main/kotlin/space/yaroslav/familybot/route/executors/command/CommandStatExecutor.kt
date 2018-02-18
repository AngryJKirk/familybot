package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.CommandByUser
import space.yaroslav.familybot.common.utils.bold
import space.yaroslav.familybot.common.utils.formatTopList
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.repos.ifaces.HistoryRepository
import space.yaroslav.familybot.route.models.Command

@Component
class CommandStatExecutor(val repository: HistoryRepository) : CommandExecutor {
    private val title = "Статистика по командам:\n".bold()

    override fun command(): Command {
        return Command.COMMAND_STATS
    }


    override fun execute(update: Update): (AbsSender) -> Unit {
        val all = repository.getAll(update.message.chat.toChat()).groupBy { it.command }

        val topList = all
                .filterNot { it.key == command() }
                .map { format(it) }
                .joinToString("\n")

        return { it.execute(SendMessage(update.message.chatId, title + topList).enableHtml(true)) }
    }

    private fun format(it: Map.Entry<Command, List<CommandByUser>>) =
            "Команда " + "${it.key.command}:".bold() + "\n" + it.value.map { it.user }.formatTopList().joinToString("\n")


}