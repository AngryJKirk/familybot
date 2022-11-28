package dev.storozhenko.familybot.executors.eventbased.keyword

import org.telegram.telegrambots.meta.bots.AbsSender
import dev.storozhenko.familybot.models.router.ExecutorContext

interface KeyWordProcessor {

    fun isRandom(context: ExecutorContext): Boolean = false

    fun canProcess(context: ExecutorContext): Boolean

    fun process(context: ExecutorContext): suspend (AbsSender) -> Unit
}
