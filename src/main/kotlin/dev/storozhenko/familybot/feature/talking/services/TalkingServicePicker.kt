package dev.storozhenko.familybot.feature.talking.services

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.ChatGPTFreeMessagesLeft
import dev.storozhenko.familybot.feature.settings.models.ChatGPTPaidTill
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.days

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

        val paidTill =
            easyKeyValueService.get(ChatGPTPaidTill, context.chatKey, Instant.now().minus(100, ChronoUnit.SECONDS))
        notifierService.notifyIfSubscriptionIsEnding(context)

        if (paidTill.isAfter(Instant.now())) {
            return chatGpt.getReplyToUser(context, shouldBeQuestion)
        }

        val amountOfFreeMessages = easyKeyValueService.get(ChatGPTFreeMessagesLeft, context.chatKey)

        return if (amountOfFreeMessages == null) {
            easyKeyValueService.put(ChatGPTFreeMessagesLeft, context.chatKey, 10, duration = 30.days)
            chatGpt.getReplyToUser(context, shouldBeQuestion)
        } else {
            if (amountOfFreeMessages > 0) {
                if (amountOfFreeMessages == 1L) {
                    notifierService.notifyThatFreeMessagesRunOut(context)
                }
                easyKeyValueService.decrement(ChatGPTFreeMessagesLeft, context.chatKey)
                chatGpt.getReplyToUser(context, shouldBeQuestion)
            } else {
                old.getReplyToUser(context, shouldBeQuestion)
            }
        }
    }


}
