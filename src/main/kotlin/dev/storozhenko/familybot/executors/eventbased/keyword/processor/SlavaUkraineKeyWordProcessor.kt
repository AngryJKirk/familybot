package dev.storozhenko.familybot.executors.eventbased.keyword.processor

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.executors.eventbased.keyword.KeyWordProcessor
import dev.storozhenko.familybot.models.router.ExecutorContext
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class SlavaUkraineKeyWordProcessor : KeyWordProcessor {

    override fun isRandom(context: ExecutorContext) = true

    override fun canProcess(context: ExecutorContext): Boolean {
        val text = context.message.text ?: return false
        return containsUkraineName(text) || containsRussianName(text)
    }

    override fun process(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val text = context.message.text
        val response = if (containsRussianName(text)) {
            "Слава Украине"
        } else {
            "Слава Україні"
        }
        return {
            it.send(context, response, replyToUpdate = true, shouldTypeBeforeSend = true)
        }
    }

    private fun containsRussianName(text: String): Boolean {
        return text.contains("Украина", ignoreCase = true)
    }

    private fun containsUkraineName(text: String): Boolean {
        return text.contains("Україна", ignoreCase = true)
    }
}
