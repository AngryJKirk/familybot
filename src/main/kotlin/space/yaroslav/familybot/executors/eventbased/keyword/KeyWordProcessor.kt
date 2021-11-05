package space.yaroslav.familybot.executors.eventbased.keyword

import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.models.router.ExecutorContext

interface KeyWordProcessor {

    fun isRandom(executorContext: ExecutorContext): Boolean = false

    fun canProcess(executorContext: ExecutorContext): Boolean

    fun process(executorContext: ExecutorContext): suspend (AbsSender) -> Unit
}
