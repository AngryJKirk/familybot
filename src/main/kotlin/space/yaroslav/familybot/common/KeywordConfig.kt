package space.yaroslav.familybot.common

import java.time.Instant
import java.time.temporal.ChronoUnit

class KeywordConfig(
        val rageMode: Boolean = false,
        var rageModeLimit: Int = 0,
        ttl: Instant = Instant.now().plus(100, ChronoUnit.DAYS),
        chat: Chat) : Config(chat, ttl)