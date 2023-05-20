package dev.storozhenko.familybot.feature.talking.services

import dev.storozhenko.familybot.common.extensions.isToday
import dev.storozhenko.familybot.common.extensions.prettyFormat
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.ChatGPTNotificationNeeded
import dev.storozhenko.familybot.feature.settings.models.ChatGPTPaidTill
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.minutes

@Component
class SubscriptionsNotifierService(private val easyKeyValueService: EasyKeyValueService) {

    companion object {
        private val scope = CoroutineScope(Dispatchers.Default)
    }

    @Scheduled(cron = "0 0 0 * * *")
    fun setUpNotification() {
        easyKeyValueService.getAllByPartKey(ChatGPTPaidTill)
            .filter { (_, date) -> date.isToday() }
            .forEach { (key, date) -> easyKeyValueService.put(ChatGPTNotificationNeeded, key, date) }
    }

    fun notifyIfSubscriptionIsEnding(context: ExecutorContext) {
        val expirationDate =
            easyKeyValueService.getAndRemove(ChatGPTNotificationNeeded, context.chatKey)
                ?: return

        scope.launch {
            val message = context.phrase(Phrase.CHAT_GTP_SUBSCRIPTION_RUN_OUT)
                .replace("$", expirationDate.prettyFormat(dateOnly = true))
            delay(1.minutes)
            context.sender.send(context, message)
        }
    }

    fun notifyThatFreeMessagesRunOut(context: ExecutorContext) {
        scope.launch {
            delay(1.minutes)
            context.sender.send(context, context.phrase(Phrase.CHAT_GTP_FREE_MESSAGES_RUN_OUT))
        }
    }
}
