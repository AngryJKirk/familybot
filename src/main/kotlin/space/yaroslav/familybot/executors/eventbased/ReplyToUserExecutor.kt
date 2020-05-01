package space.yaroslav.familybot.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.FunctionId
import space.yaroslav.familybot.models.Priority
import space.yaroslav.familybot.services.TalkingService
import space.yaroslav.familybot.telegram.BotConfig

@Component
class ReplyToUserExecutor(
    private val botConfig: BotConfig,
    private val talkingService: TalkingService
) : Executor, Configurable {

    override fun getFunctionId(): FunctionId {
        return FunctionId.CHATTING
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val reply = talkingService.getReplyToUser(update)
        return {
            it.send(update, reply, replyToUpdate = true, shouldTypeBeforeSend = true)
        }
    }

    override fun canExecute(message: Message): Boolean {
        return isReplyToBot(message) || isBotMention(message)
    }

    override fun priority(update: Update): Priority {
        return Priority.VERY_LOW
    }

    private fun isBotMention(message: Message): Boolean {
        return message.text?.contains("@${botConfig.botname}") ?: false
    }

    private fun isReplyToBot(message: Message): Boolean {
        return message.isReply && message.replyToMessage.from.userName == botConfig.botname
    }
}
