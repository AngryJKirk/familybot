package dev.storozhenko.familybot.feature.talking.services

import dev.storozhenko.familybot.common.extensions.isToday
import dev.storozhenko.familybot.common.extensions.prettyFormat
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.ChatGPTNotificationNeeded
import dev.storozhenko.familybot.feature.settings.models.ChatGPTPaidTill
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import kotlin.time.Duration.Companion.minutes

@Component
class SubscriptionsNotifierService(private val easyKeyValueService: EasyKeyValueService) {

    @Scheduled(cron = "0 0 0 * * *")
    fun setUpNotification() {
        easyKeyValueService.getAllByPartKey(ChatGPTPaidTill)
            .map { (key, value) -> key to Instant.ofEpochSecond(value) }
            .filter { (_, date) -> date.isToday() }
            .forEach { (key, date) -> easyKeyValueService.put(ChatGPTNotificationNeeded, key, date.epochSecond) }
    }

    suspend fun notifyIfSubscriptionIsEnding(context: ExecutorContext) {
        coroutineScope {
            val expirationDate =
                easyKeyValueService.getAndRemove(ChatGPTNotificationNeeded, context.chatKey)?.let(Instant::ofEpochSecond)
                    ?: return@coroutineScope
            val message = context.phrase(Phrase.CHAT_GTP_SUBSCRIPTION_RUN_OUT)
                .replace("$", expirationDate.prettyFormat(dateOnly = true))
            delay(1.minutes)
            context.sender.send(context, message)
        }
    }

}