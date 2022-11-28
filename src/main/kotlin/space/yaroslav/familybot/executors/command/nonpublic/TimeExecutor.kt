package space.yaroslav.familybot.executors.command.nonpublic

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.*
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.bold
import space.yaroslav.familybot.common.extensions.code
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.telegram.FamilyBot
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class TimeExecutor : CommandExecutor() {
    private val restTemplate = RestTemplate()

    private val inputDateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm")


    companion object {
        private const val timeZoneUrl = "https://worldtimeapi.org/api/timezone/"
        private val times = mapOf(
            "Время в Лондоне:   " to "Europe/London",
            "Время в Москве:    " to "Europe/Moscow",
            "Время в Аргентине: " to "America/Argentina/Buenos_Aires",
            "Время в Малайзии:  " to "Asia/Kuala_Lumpur",
        )
    }

    override fun command() = Command.TIME

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        return {
            val result =
                times
                    .map { (prefix, zone) -> prefix.code() to callApiAsync(zone) }
                    .map { (prefix, result) -> prefix + result.await() }
                    .joinToString(separator = "\n")
            it.send(context, result, replyToUpdate = true, enableHtml = true)
        }
    }

    private suspend fun callApiAsync(zone: String): Deferred<String> {
        return coroutineScope {
            async {
                runCatching {
                    val response = withContext(Dispatchers.IO) {
                        restTemplate
                            .exchange(timeZoneUrl + zone, HttpMethod.GET, null, DateTimeServiceResponse::class.java)
                    }
                        .body ?: throw FamilyBot.InternalException("Can't deserialize response from date time API")

                    LocalDateTime.parse(response.datetime, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                        .format(inputDateTimeFormatter).bold()
                }.getOrDefault("Абонент недоступен...")
            }
        }
    }

    private class DateTimeServiceResponse(@JsonProperty("datetime") val datetime: String)


}