package space.yaroslav.familybot.route

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.bold
import space.yaroslav.familybot.common.italic
import space.yaroslav.familybot.common.toChat
import space.yaroslav.familybot.repos.CommonRepository

@Component
class PidorStatsExecutor(val repository: CommonRepository) : CommandExecutor() {

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