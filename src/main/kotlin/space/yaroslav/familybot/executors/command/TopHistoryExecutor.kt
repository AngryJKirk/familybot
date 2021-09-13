package space.yaroslav.familybot.executors.command

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.parseJson
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.telegram.BotConfig

@Component
class TopHistoryExecutor(config: BotConfig) :
    CommandExecutor(config) {
    private val log = getLogger()
    private val lazyMamoeb: Lazy<Mamoeb?> = lazy {
        runCatching {
            RestTemplate()
                .getForEntity(
                    "https://raw.githubusercontent.com/Mi7teR/mamoeb3000/master/templates.json",
                    String::class.java
                ).body
                ?.parseJson<Mamoeb>()
        }
            .getOrElse {
                log.error("Can't get mamoeb", it)
                null
            }
    }

    override fun command(): Command {
        return Command.TOP_HISTORY
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val mamoeb = lazyMamoeb.value ?: return {}

        return { sender -> sender.send(update, mamoeb.curses.random()) }
    }
}

data class Mamoeb(
    @JsonProperty("curses") val curses: List<String>
)
