package space.yaroslav.familybot.executors.command.nonpublic

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.bold
import space.yaroslav.familybot.common.extensions.code
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.Command
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Component
class TimeExecutor : CommandExecutor() {

    private val inputDateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm")


    companion object {
        private val times = mapOf(
            "Время в Лондоне:    " to "Europe/London",
            "Время в Москве:     " to "Europe/Moscow",
            "Время в Ульяновске: " to "Europe/Samara",
            "Время в Аргентине:  " to "America/Argentina/Buenos_Aires",
            "Время в Малайзии:   " to "Asia/Kuala_Lumpur",
        )
            .map { (prefix, zone) -> prefix.code() to ZoneId.of(zone) }
            .toMap()
    }

    override fun command() = Command.TIME

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val now = Instant.now()
        val result = times.map { (prefix, zone) -> prefix + getDateString(now, zone) }
            .joinToString(separator = "\n")
        return {
            it.send(context, result, replyToUpdate = true, enableHtml = true)
        }
    }

    private fun getDateString(now: Instant, zone: ZoneId): String {
        return now.atZone(zone).format(inputDateTimeFormatter).bold()
    }

}