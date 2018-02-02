package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.bold
import space.yaroslav.familybot.common.formatTopList
import space.yaroslav.familybot.common.toChat
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.route.models.Command
import java.time.*

@Component
class PidorStatsMonthExecutor(val repository: CommonRepository) : CommandExecutor() {
    override fun command(): Command {
        return Command.STATS_MONTH
    }

    val monthMap = mapOf(
        Month.JANUARY to "январь",
        Month.FEBRUARY to "февраль",
        Month.MARCH to "март",
        Month.APRIL to "апрель",
        Month.MAY to "май",
        Month.JUNE to "июнь",
        Month.JULY to "июль",
        Month.AUGUST to "август",
        Month.SEPTEMBER to "сентябрь",
        Month.OCTOBER to "октябрь",
        Month.NOVEMBER to "ноябрь",
        Month.DECEMBER to "декабрь"
            )

    override fun execute(update: Update): (AbsSender) -> Unit {
        val now = LocalDate.now()
        val pidorsByChat = repository.getPidorsByChat(update.message.chat.toChat(),
                startDate = LocalDateTime.of(LocalDate.of(now.year, now.month, 1), LocalTime.MIDNIGHT)
                        .toInstant(ZoneOffset.UTC))
        val formatPidors = formatTopList(pidorsByChat.map { it.user })
        now.month
        val title = "Топ пидоров за ${monthMap[now.month]}:\n".bold()
        return { it.execute(SendMessage(update.message.chatId, title + formatPidors.joinToString("\n")).enableHtml(true)) }
    }



}