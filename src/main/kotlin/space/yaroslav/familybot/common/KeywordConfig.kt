package space.yaroslav.familybot.common

import java.time.Instant
import java.time.temporal.ChronoUnit

class KeywordConfig(val randomPower: Int = 5,
                    val rageMode: Boolean = false,
                    val ttl: Instant = Instant.now().plus(1, ChronoUnit.DAYS)) : Config()