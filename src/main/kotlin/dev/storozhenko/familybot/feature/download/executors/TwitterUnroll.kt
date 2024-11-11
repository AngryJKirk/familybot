package dev.storozhenko.familybot.feature.download.executors

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.Executor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority
import dev.storozhenko.familybot.feature.settings.models.TwitterUnroll
import org.springframework.stereotype.Component

@Component
class TwitterUnroll(
    private val easyKeyValueService: EasyKeyValueService
) : Executor {
    override suspend fun execute(context: ExecutorContext) {
        val message = getTwitterUrls(context)
            .mapNotNull(::extractStatusId)
            .joinToString(separator = "\n") { "https://unrollnow.com/status/$it" }
        context.client.send(context, message, replyToUpdate = true)
    }

    override fun canExecute(context: ExecutorContext): Boolean {
        if (!easyKeyValueService.get(TwitterUnroll, context.chatKey, false)) return false

        if (context.message.entities?.isEmpty() == true) return false

        return getTwitterUrls(context).isNotEmpty()
    }

    override fun priority(context: ExecutorContext) = Priority.VERY_LOW

    private fun getTwitterUrls(context: ExecutorContext): List<String> {
        return context.message.entities
            .filter { entity -> entity.type == "url" }
            .map { entity -> entity.text }
            .filter { url -> (url.contains("twitter.com") || url.contains("x.com")) && url.contains("/status/") }
    }

    fun extractStatusId(url: String): String? {
        val regex = """https?://(?:x\.com|twitter\.com)/\w+/status/(\d+)""".toRegex()
        return regex.find(url)?.groupValues?.get(1)
    }
}