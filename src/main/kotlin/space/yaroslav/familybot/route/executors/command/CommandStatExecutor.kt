package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.bold
import space.yaroslav.familybot.common.formatTopList
import space.yaroslav.familybot.common.toChat
import space.yaroslav.familybot.repos.ifaces.HistoryRepository
import space.yaroslav.familybot.route.models.Command

@Component
class CommandStatExecutor(val repository: HistoryRepository) : CommandExecutor() {
    override fun command(): Command {
        return Command.COMMAND_STATS
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val all = repository.getAll(update.message.chat.toChat()).groupBy { it.command }
        val title = "Статистика по командам:\n".bold()
        val topList = all.filterNot { it.key == command() } .map { "Команда " + "${it.key.command}:".bold() + "\n" + formatTopList(it.value.map { it.user }).joinToString("\n") }
                .joinToString("\n")
        return { it.execute(SendMessage(update.message.chatId, title + topList).enableHtml(true)) }
    }


}