package dev.storozhenko.familybot.feature.tribute

import com.fasterxml.jackson.annotation.JsonProperty
import dev.storozhenko.familybot.common.extensions.parseJson
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.telegram.FamilyBot
import org.apache.commons.codec.binary.Base64
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class TopHistoryExecutor : CommandExecutor() {
    companion object {
        val mamoeb: Mamoeb = this::class.java.classLoader
            .getResourceAsStream("static/curses")
            ?.readAllBytes()
            ?.let { Base64.decodeBase64(it) }
            ?.decodeToString()
            ?.parseJson<Mamoeb>()
            ?: throw FamilyBot.InternalException("curses is missing")

    }

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
