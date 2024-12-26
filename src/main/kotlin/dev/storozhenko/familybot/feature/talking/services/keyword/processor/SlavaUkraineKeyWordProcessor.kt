package dev.storozhenko.familybot.feature.talking.services.keyword.processor


import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.talking.services.keyword.KeyWordProcessor
import org.springframework.stereotype.Component

@Component
class SlavaUkraineKeyWordProcessor : KeyWordProcessor {

    override fun isRandom() = true

    override fun canProcess(context: ExecutorContext): Boolean {
        val text = context.message.text ?: return false
        return containsUkraineName(text) || containsRussianName(text)
    }

    override suspend fun process(context: ExecutorContext) {
        val text = context.message.text
        val response = if (containsRussianName(text)) {
            "Слава Украине"
        } else {
            "Слава Україні"
        }
        context.send(response, replyToUpdate = true, shouldTypeBeforeSend = true)
    }

    private fun containsRussianName(text: String): Boolean {
        return text.contains("Украина", ignoreCase = true)
    }

    private fun containsUkraineName(text: String): Boolean {
        return text.contains("Україна", ignoreCase = true)
    }
}
