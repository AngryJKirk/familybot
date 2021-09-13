package space.yaroslav.familybot.executors.eventbased.keyword

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.common.extensions.randomBoolean
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.toChat
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

    override fun canProcess(message: Message): Boolean {
        return isReplyToBot(message) || isBotMention(message) || isBotNameMention(message)
    }

    override fun process(update: Update): suspend (AbsSender) -> Unit {
        if (isFuckOff(update)) {
            return fuckOff(update)
        }
        val shouldBeQuestion = isBotMention(update.message) || isBotNameMention(update.message)
        return {
            val reply = talkingService.getReplyToUser(
                update,
                randomBoolean() && shouldBeQuestion
            )
            it.send(update, reply, replyToUpdate = true, shouldTypeBeforeSend = true)
        }
    }

    private fun isBotMention(message: Message): Boolean {
        return message.text?.contains("@${botConfig.botName}") ?: false
    }

    private fun isBotNameMention(message: Message): Boolean {
        return message.text?.contains("сучар", ignoreCase = true) ?: false
    }

    private fun isReplyToBot(message: Message): Boolean {
        return message.isReply && message.replyToMessage.from.userName == botConfig.botName
    }

    fun isFuckOff(update: Update): Boolean {
        val text = update.message.text ?: return false
        return if (!isUserUnderTolerance(update)) {
            fuckOffPhrases.any { it.matches(text) }
        } else {
            false
        }
    }

    fun fuckOff(update: Update): suspend (AbsSender) -> Unit {
        easyKeyValueService.put(FuckOffOverride, update.toChat().key(), true, defaultFuckOffDuration)
        easyKeyValueService.put(FuckOffTolerance, update.key(), true, defaultToleranceDuration)
        return {}
    }

    private fun isUserUnderTolerance(update: Update) =
        easyKeyValueService.get(FuckOffTolerance, update.key(), defaultValue = false)
}
