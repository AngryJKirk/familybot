package space.yaroslav.familybot.executors.eventbased.keyword

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.services.TalkingService
import space.yaroslav.familybot.telegram.BotConfig

@Component
class BotMentionKeyWordProcessor(
    private val botConfig: BotConfig,
    private val talkingService: TalkingService
) : KeyWordProcessor {

    override fun canProcess(message: Message): Boolean {
        return isReplyToBot(message) || isBotMention(message)
    }

    override fun process(update: Update): suspend (AbsSender) -> Unit {
        val reply = talkingService.getReplyToUser(update)
        return {
            it.send(update, reply, replyToUpdate = true, shouldTypeBeforeSend = true)
        }
    }

    private fun isBotMention(message: Message): Boolean {
        return message.text?.contains("@${botConfig.botname}") ?: false
    }

    private fun isReplyToBot(message: Message): Boolean {
        return message.isReply && message.replyToMessage.from.userName == botConfig.botname
    }
}
