package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.utils.bold
import space.yaroslav.familybot.common.utils.formatTopList
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.FunctionId
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.ZoneOffset

@Component
class PidorStatsWorldExecutor(val repository: CommonRepository) : CommandExecutor, Configurable {
    override fun getFunctionId(): FunctionId {
        return FunctionId.PIDOR
    }

    override fun command(): Command {
        return Command.STATS_WORLD
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val pidorsByChat = repository.getAllPidors(
            startDate = LocalDateTime.of(
                LocalDate.of(2000, Month.JANUARY, 1),
                LocalTime.MIDNIGHT
            ).toInstant(ZoneOffset.UTC)
        )
            .map { it.user }
            .formatTopList()
        val title = "Топ пидоров всего мира за все время:\n".bold()
        return {
            it.execute(
                SendMessage(
                    update.message.chatId,
                    title + pidorsByChat.joinToString("\n")
                ).enableHtml(true)
            )
        }
    }
}