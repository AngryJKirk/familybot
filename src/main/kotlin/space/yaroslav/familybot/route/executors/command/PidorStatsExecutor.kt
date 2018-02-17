package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.bold
import space.yaroslav.familybot.common.utils.italic
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.FunctionId

@Component
class PidorStatsExecutor(val repository: CommonRepository) : CommandExecutor(), Configurable {

    override fun getFunctionId(): FunctionId {
        return FunctionId.PIDOR
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val chat = update.message.chat.toChat()
        val pidorsByChat = repository.getPidorsByChat(chat)
        val groupBy = pidorsByChat
                .groupBy { it.user }
                .map { it.key to it.value.size }
                .sortedByDescending { it.second }
                .mapIndexed { index, pair -> format(index, pair) }
        val title = "Топ пидоров за все время:\n".bold()
        return { it.execute(SendMessage(update.message.chatId, title + groupBy.joinToString("\n")).enableHtml(true)) }

    }

    override fun command(): Command {
        return Command.STATS_TOTAL
    }

    private fun format(index: Int, pidorStats: Pair<User, Int>): String {

        val generalName = pidorStats.first.name ?: pidorStats.first.nickname
        val i = "${index + 1}.".bold()
        val stat = "${pidorStats.second} раз(а)".italic()
        return "$i $generalName — $stat"
    }
}