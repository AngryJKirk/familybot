package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.bold
import space.yaroslav.familybot.common.utils.formatTopList
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.services.dictionary.Dictionary
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.ZoneOffset

@Component
class PidorStatsWorldExecutor(
    val repository: CommonRepository,
    val dictionary: Dictionary
) : CommandExecutor, Configurable {
    override fun getFunctionId(): FunctionId {
        return FunctionId.PIDOR
    }

    override fun command(): Command {
        return Command.STATS_WORLD
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val pidorsByChat = repository.getAllPidors(
            startDate = LocalDateTime.of(
                LocalDate.of(2000, Month.JANUARY, 1),
                LocalTime.MIDNIGHT
            ).toInstant(ZoneOffset.UTC)
        )
            .map { it.user }
            .formatTopList()
            .take(100)

        val title = "${dictionary.get(Phrase.PIDOR_STAT_WORLD)}:\n".bold()
        return { it.send(update, title + pidorsByChat.joinToString("\n"), enableHtml = true) }
    }
}
