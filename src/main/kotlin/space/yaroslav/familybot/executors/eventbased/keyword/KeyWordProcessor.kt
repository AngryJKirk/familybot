package space.yaroslav.familybot.executors.eventbased.keyword

import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender

interface KeyWordProcessor {

    fun isRandom(message: Message): Boolean = false

    fun canProcess(message: Message): Boolean

    fun process(update: Update): suspend (AbsSender) -> Unit
}
