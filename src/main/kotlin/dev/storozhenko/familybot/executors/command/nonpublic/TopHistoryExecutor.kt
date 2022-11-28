package space.yaroslav.familybot.executors.command.nonpublic

import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.commons.codec.binary.Base64
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.parseJson
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.telegram.FamilyBot

@Component
class TopHistoryExecutor : CommandExecutor() {
    private val mamoeb: Mamoeb = this::class.java.classLoader
        .getResourceAsStream("static/curses")
        ?.readAllBytes()
        ?.let { Base64.decodeBase64(it) }
        ?.decodeToString()
        ?.parseJson<Mamoeb>()
        ?: throw FamilyBot.InternalException("curses is missing")

    override fun command(): Command {
        return Command.TOP_HISTORY
    }

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        return { sender -> sender.send(context, mamoeb.curses.random()) }
    }
}

data class Mamoeb(
    @JsonProperty("Templates") val curses: List<String>
)
