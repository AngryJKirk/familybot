package dev.storozhenko.familybot.services.talking

import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.services.settings.ChatGPTFreeMessagesLeft
import dev.storozhenko.familybot.services.settings.ChatGPTPaidTill
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.telegram.BotConfig
import org.springframework.stereotype.Component
import java.time.Instant
import kotlin.time.Duration.Companion.days

@Component("Picker")
class TalkingServicePicker(
    private val chatGpt: TalkingServiceChatGpt,
    private val old: TalkingServiceOld,
    private val easyKeyValueService: EasyKeyValueService,
    private val botConfig: BotConfig
) : TalkingService {

    override suspend fun getReplyToUser(context: ExecutorContext, shouldBeQuestion: Boolean): String {
        if (botConfig.openAiToken == null) {
            return old.getReplyToUser(context, shouldBeQuestion)
        }

        val paidTill = easyKeyValueService.get(ChatGPTPaidTill, context.chatKey, Instant.now().epochSecond - 100)

        if (Instant.ofEpochSecond(paidTill).isAfter(Instant.now())) {
            return chatGpt.getReplyToUser(context, shouldBeQuestion)
        }

        val amountOfFreeMessages = easyKeyValueService.get(ChatGPTFreeMessagesLeft, context.chatKey)

        return if (amountOfFreeMessages == null) {
            easyKeyValueService.put(ChatGPTFreeMessagesLeft, context.chatKey, 30, duration = 30.days)
            chatGpt.getReplyToUser(context, shouldBeQuestion)
        } else {
            if (amountOfFreeMessages > 0) {
                easyKeyValueService.decrement(ChatGPTFreeMessagesLeft, context.chatKey)
                chatGpt.getReplyToUser(context, shouldBeQuestion)
            } else {
                old.getReplyToUser(context, shouldBeQuestion)
            }
        }
    }
}