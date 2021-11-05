package space.yaroslav.familybot.executors.eventbased.keyword.processor

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.randomBoolean
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.executors.eventbased.keyword.KeyWordProcessor
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.FuckOffOverride
import space.yaroslav.familybot.services.settings.FuckOffTolerance
import space.yaroslav.familybot.services.talking.TalkingService
import space.yaroslav.familybot.telegram.BotConfig
import java.time.Duration

@Component
class BotMentionKeyWordProcessor(
    private val botConfig: BotConfig,
    private val talkingService: TalkingService,
    private val easyKeyValueService: EasyKeyValueService
) : KeyWordProcessor {

    private val defaultFuckOffDuration = Duration.ofMinutes(15)
    private val defaultToleranceDuration = Duration.ofHours(24)

    private val fuckOffPhrases = setOf(
        Regex(".*завали.{0,10}ебало.*", RegexOption.IGNORE_CASE),
        Regex(".*ебало.{0,10}завали.*", RegexOption.IGNORE_CASE),
        Regex(".*стули.{0,10}пельку.*", RegexOption.IGNORE_CASE),
        Regex(".*пельку.{0,10}стули.*", RegexOption.IGNORE_CASE)
    )

    override fun canProcess(executorContext: ExecutorContext): Boolean {
        val message = executorContext.message
        return isReplyToBot(message) || isBotMention(message) || isBotNameMention(message)
    }

    override fun process(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {
        if (isFuckOff(executorContext)) {
            return fuckOff(executorContext)
        }
        val shouldBeQuestion = isBotMention(executorContext.message) || isBotNameMention(executorContext.message)
        return {
            val reply = talkingService.getReplyToUser(
                executorContext,
                randomBoolean() && shouldBeQuestion
            )
            it.send(executorContext, reply, replyToUpdate = true, shouldTypeBeforeSend = true)
        }
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

    fun isFuckOff(executorContext: ExecutorContext): Boolean {
        val text = executorContext.message.text ?: return false
        return if (!isUserUnderTolerance(executorContext)) {
            fuckOffPhrases.any { it.matches(text) }
        } else {
            false
        }
    }

    fun fuckOff(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {
        easyKeyValueService.put(FuckOffOverride, executorContext.chatKey, true, defaultFuckOffDuration)
        easyKeyValueService.put(FuckOffTolerance, executorContext.userAndChatKey, true, defaultToleranceDuration)
        return {}
    }

    private fun isUserUnderTolerance(executorContext: ExecutorContext) =
        easyKeyValueService.get(FuckOffTolerance, executorContext.userAndChatKey, defaultValue = false)
}
