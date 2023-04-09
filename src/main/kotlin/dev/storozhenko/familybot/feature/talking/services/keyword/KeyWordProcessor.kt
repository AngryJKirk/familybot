package dev.storozhenko.familybot.feature.talking.services.keyword

import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import org.telegram.telegrambots.meta.bots.AbsSender

interface KeyWordProcessor {

    fun isRandom(context: ExecutorContext): Boolean = false

    fun canProcess(context: ExecutorContext): Boolean

    fun process(context: ExecutorContext): suspend (AbsSender) -> Unit
}
