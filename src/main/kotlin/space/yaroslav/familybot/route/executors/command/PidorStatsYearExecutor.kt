package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.utils.bold
import space.yaroslav.familybot.common.utils.formatTopList
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.FunctionId
import java.time.*

@Component
class PidorStatsYearExecutor(val repository: CommonRepository) : CommandExecutor(), Configurable {
    override fun getFunctionId(): FunctionId {
        return FunctionId.PIDOR
    }
    override fun command(): Command {
        return Command.STATS_YEAR
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val now = LocalDate.now()
        val pidorsByChat = repository.getPidorsByChat(update.message.chat.toChat(),
                startDate = LocalDateTime.of(LocalDate.of(now.year, Month.JANUARY, 1), LocalTime.MIDNIGHT).toInstant(ZoneOffset.UTC))
        val formatPidors = pidorsByChat
                .map { it.user }
                .formatTopList()
        val title = "Топ пидоров за ${now.year} год:\n".bold()
        return { it.execute(SendMessage(update.message.chatId, title + formatPidors.joinToString("\n")).enableHtml(true)) }
    }
}