package space.yaroslav.familybot.repos

import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.KeywordConfig
import space.yaroslav.familybot.repos.ifaces.RagemodeRepository
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class InCodeConfigRepository : RagemodeRepository {

    private val configs: MutableMap<Chat, KeywordConfig> = HashMap()

    override fun enable(minutes: Int, messages: Int, chat: Chat) {
        configs[chat] = KeywordConfig(
            rageMode = true,
            rageModeLimit = messages,
            ttl = Instant.now().plus(minutes.toLong(), ChronoUnit.MINUTES),
            chat = chat
        )
    }

    override fun isEnabled(chat: Chat): Boolean {
        return configs[chat]?.let { it.rageMode && it.rageModeLimit > 0 && Instant.now().isBefore(it.ttl) } ?: false
    }

    override fun decrement(chat: Chat) {
        configs[chat]?.let { it.rageModeLimit-- }
    }
}