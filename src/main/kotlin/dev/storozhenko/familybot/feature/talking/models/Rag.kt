package dev.storozhenko.familybot.feature.talking.models

import java.time.Instant

enum class Kind { SEMANTIC, KEYWORD_RU, KEYWORD_SIMPLE, FUZZY, RECENT }

data class RagHit(
    val ragId: Long,
    val msgId: Long,
    val userId: Long,
    val ts: Instant,
    val text: String,
    val score: Double,
    val kind: Kind,
)
