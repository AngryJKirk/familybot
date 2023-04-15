package dev.storozhenko.familybot.feature.talking.services.keyword.processor

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.randomBoolean
import dev.storozhenko.familybot.common.extensions.sendDeferred
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.FuckOffOverride
import dev.storozhenko.familybot.feature.settings.models.FuckOffTolerance
import dev.storozhenko.familybot.feature.talking.services.TalkingService
import dev.storozhenko.familybot.feature.talking.services.keyword.KeyWordProcessor
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Component
class BotMentionKeyWordProcessor(
    private val botConfig: BotConfig,
    @Qualifier("Picker") private val talkingService: TalkingService,
    private val easyKeyValueService: EasyKeyValueService
) : KeyWordProcessor {

    private val defaultFuckOffDuration = 15.minutes
    private val defaultToleranceDuration = 24.hours

    private val fuckOffPhrases = setOf(
        Regex(".*завали.{0,10}ебало.*", RegexOption.IGNORE_CASE),
        Regex(".*ебало.{0,10}завали.*", RegexOption.IGNORE_CASE),
        Regex(".*стули.{0,10}пельку.*", RegexOption.IGNORE_CASE),
        Regex(".*пельку.{0,10}стули.*", RegexOption.IGNORE_CASE)
    )

    override fun canProcess(context: ExecutorContext): Boolean {
        val message = context.message
        return isReplyToBot(message) || isBotMention(message) || isBotNameMention(message)
    }

    override suspend fun process(context: ExecutorContext) {
        if (isFuckOff(context)) {
            fuckOff(context)
            return
        }
        val shouldBeQuestion = isBotMention(context.message) || isBotNameMention(context.message)
        if (isMediaResponse(context.message)) {
            return
        }
        coroutineScope {
            val reply = async {
                talkingService.getReplyToUser(
                    context,
                    randomBoolean() && shouldBeQuestion
                )
            }
            context.sender.sendDeferred(
                context,
                reply,
                replyToUpdate = true,
                shouldTypeBeforeSend = true,
                enableHtml = true
            )
        }
    }

    private fun isMediaResponse(message: Message): Boolean {
        val replyToMessage = message.replyToMessage ?: return false
        return replyToMessage.hasVideo() || replyToMessage.hasDocument()
            || replyToMessage.hasPhoto()
    }

    private fun isBotMention(message: Message): Boolean {
        return message.text?.contains("@${botConfig.botName}") ?: false
    }

    private fun isBotNameMention(message: Message): Boolean {
        val text = message.text ?: return false

        return botConfig
            .botNameAliases.any { alias -> text.contains(alias, ignoreCase = true) }
    }

    private fun isReplyToBot(message: Message): Boolean {
        return message.isReply && message.replyToMessage.from.userName == botConfig.botName
    }

    fun isFuckOff(context: ExecutorContext): Boolean {
        val text = context.message.text ?: return false
        return if (!isUserUnderTolerance(context)) {
            fuckOffPhrases.any { it.matches(text) }
        } else {
            false
        }
    }

    fun fuckOff(context: ExecutorContext) {
        easyKeyValueService.put(FuckOffOverride, context.chatKey, true, defaultFuckOffDuration)
        easyKeyValueService.put(FuckOffTolerance, context.userAndChatKey, true, defaultToleranceDuration)
    }

    private fun isUserUnderTolerance(context: ExecutorContext) =
        easyKeyValueService.get(FuckOffTolerance, context.userAndChatKey, defaultValue = false)
}
