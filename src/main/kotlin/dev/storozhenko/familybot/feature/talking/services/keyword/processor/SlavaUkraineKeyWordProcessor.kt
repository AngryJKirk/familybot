package dev.storozhenko.familybot.feature.talking.services.keyword.processor

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.talking.services.keyword.KeyWordProcessor
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
