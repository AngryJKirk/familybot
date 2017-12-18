package space.yaroslav.familybot.route

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.bold
import space.yaroslav.familybot.common.formatPidors
import space.yaroslav.familybot.common.toChat
import space.yaroslav.familybot.repos.CommonRepository
import java.time.*

@Component
class PidorStatsMonthExecutor(val repository: CommonRepository) : CommandExecutor() {
    override fun command(): Command {
        return Command.STATS_MONTH
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val pidorsByChat = repository.getPidorsByChat(update.message.chat.toChat(),
                startDate = LocalDateTime.of(LocalDate.of(LocalDate.now().year, Month.JANUARY, 1), LocalTime.MIDNIGHT).toInstant(ZoneOffset.UTC))
        val formatPidors = formatPidors(pidorsByChat)
        val title = "Топ пидоров за месяц:\n".bold()
        return { it.execute(SendMessage(update.message.chatId, title + formatPidors.joinToString("\n")).enableHtml(true)) }
    }
}