package space.yaroslav.familybot.executors.eventbased.keyword

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.key
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.models.FunctionId
import space.yaroslav.familybot.services.TalkingService
import space.yaroslav.familybot.services.settings.EasySettingsService
import space.yaroslav.familybot.services.settings.FuckOffTolerance
import space.yaroslav.familybot.telegram.BotConfig
import java.time.Duration

@Component
class BotMentionKeyWordProcessor(
    private val botConfig: BotConfig,
    private val talkingService: TalkingService,
    private val easySettingsService: EasySettingsService
) : KeyWordProcessor {

    private val defaultFuckOffDuration = Duration.ofMinutes(15)
    private val defaultToleranceDuration = Duration.ofHours(24)

    private val fuckOffPhrases = setOf(
        Regex(".*завали.{0,10}ебало.*", RegexOption.IGNORE_CASE),
        Regex(".*ебало.{0,10}завали.*", RegexOption.IGNORE_CASE)
    )

    override fun canProcess(message: Message): Boolean {
        return isReplyToBot(message) || isBotMention(message) || isBotNameMention(message)
    }

    override fun process(update: Update): suspend (AbsSender) -> Unit {
        if (isFuckOff(update)) {
            return fuckOff(update)
        }
        val reply = talkingService.getReplyToUser(update)
        return {
            it.send(update, reply, replyToUpdate = true, shouldTypeBeforeSend = true)
        }
    }

    private fun isBotMention(message: Message): Boolean {
        return message.text?.contains("@${botConfig.botname}") ?: false
    }

    private fun isBotNameMention(message: Message): Boolean {
        return message.text?.contains("сучар", ignoreCase = true) ?: false
    }

    private fun isReplyToBot(message: Message): Boolean {
        return message.isReply && message.replyToMessage.from.userName == botConfig.botname
    }

    fun isFuckOff(update: Update): Boolean {
        return if (!isUserUnderTolerance(update)) {
            fuckOffPhrases.any { it.matches(update.message.text) }
        } else {
            false
        }
    }

    fun fuckOff(update: Update): suspend (AbsSender) -> Unit {
        setOf(
            FunctionId.TALK_BACK,
            FunctionId.GREETINGS,
            FunctionId.CHATTING,
            FunctionId.HUIFICATE
        )
            .forEach { function ->
                easySettingsService.put(
                    function.easySetting,
                    update.toChat().key(),
                    false,
                    defaultFuckOffDuration
                )
            }
        easySettingsService.put(FuckOffTolerance, update.key(), true, defaultToleranceDuration)
        return {}
    }

    private fun isUserUnderTolerance(update: Update) =
        easySettingsService.get(FuckOffTolerance, update.key(), defaultValue = false)
}
