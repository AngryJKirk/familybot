package dev.storozhenko.familybot.feature.story

data class PollResults(
    val pollId: String,
    val pollResults: MutableMap<Int, MutableSet<Long>> = mutableMapOf()
)