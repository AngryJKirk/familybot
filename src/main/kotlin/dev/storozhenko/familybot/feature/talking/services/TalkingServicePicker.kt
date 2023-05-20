package dev.storozhenko.familybot.feature.talking.services

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.ChatGPTFreeMessagesLeft
import dev.storozhenko.familybot.feature.settings.models.ChatGPTPaidTill
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import java.time.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

@Component("Picker")
class TalkingServicePicker(
    private val chatGpt: TalkingServiceChatGpt,
    private val old: TalkingServiceOld,
    private val easyKeyValueService: EasyKeyValueService,
    private val notifierService: SubscriptionsNotifierService,
    private val botConfig: BotConfig
) : TalkingService {

    override suspend fun getReplyToUser(context: ExecutorContext, shouldBeQuestion: Boolean): String {
        if (botConfig.openAiToken == null) {
            return old.getReplyToUser(context, shouldBeQuestion)
        }

        val paidTill = easyKeyValueService.get(ChatGPTPaidTill, context.chatKey, Instant.now().epochSecond - 100)
        notifierService.notifyIfSubscriptionIsEnding(context)

        if (Instant.ofEpochSecond(paidTill).isAfter(Instant.now())) {
            return chatGpt.getReplyToUser(context, shouldBeQuestion)
        }

        val amountOfFreeMessages = easyKeyValueService.get(ChatGPTFreeMessagesLeft, context.chatKey)

        return if (amountOfFreeMessages == null) {
            easyKeyValueService.put(ChatGPTFreeMessagesLeft, context.chatKey, 10, duration = 30.days)
            chatGpt.getReplyToUser(context, shouldBeQuestion)
        } else {
            if (amountOfFreeMessages > 0) {
                if (amountOfFreeMessages == 1L) {
                    notifyThatMessagesRunOut(context)
                }
                easyKeyValueService.decrement(ChatGPTFreeMessagesLeft, context.chatKey)
                chatGpt.getReplyToUser(context, shouldBeQuestion)
            } else {
                old.getReplyToUser(context, shouldBeQuestion)
            }
        }
    }

    private suspend fun notifyThatMessagesRunOut(context: ExecutorContext) {
        coroutineScope {
            launch {
                delay(1.minutes)
                context.sender.send(context, context.phrase(Phrase.CHAT_GTP_FREE_MESSAGES_RUN_OUT))
            }
        }
    }
}
