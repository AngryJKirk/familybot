package space.yaroslav.familybot.executors.eventbased

import java.util.concurrent.ThreadLocalRandom
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.chatId
import space.yaroslav.familybot.common.utils.random
import space.yaroslav.familybot.common.utils.randomNotNull
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.FunctionId
import space.yaroslav.familybot.models.Priority
import space.yaroslav.familybot.repos.ifaces.ChatLogRepository
import space.yaroslav.familybot.telegram.BotConfig

@Component
class ReplyToUserExecutor(
    private val keyset: ChatLogRepository,
    private val botConfig: BotConfig
) : Executor, Configurable {

    override fun getFunctionId(): FunctionId {
        return FunctionId.CHATTING
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val string = getRandomUserMessage(update) ?: getSmallRandomMessage(keyset.getAll())
        return {
            it.execute(SendChatAction(update.chatId(), "typing"))
            delay(ThreadLocalRandom.current().nextLong(1000, 2000))
            it.send(update, string, replyToUpdate = true) }
    }

    override fun canExecute(message: Message): Boolean {
        return isReplyToBot(message) || isBotMention(message)
    }

    override fun priority(update: Update): Priority {
        return Priority.VERY_LOW
    }

    private fun getRandomUserMessage(update: Update) =
        keyset.get(update.toUser()).takeIf { it.size > 300 }?.random()

    private fun getSmallRandomMessage(messages: List<String>): String {
        var message: String = messages.randomNotNull()
        while (message.split(" ").size >= 10) {
            message = messages.randomNotNull()
        }
        return message
    }

    private fun isBotMention(message: Message): Boolean {
        return message.text?.contains("@${botConfig.botname}") ?: false
    }

    private fun isReplyToBot(message: Message): Boolean {
        return message.isReply && message.replyToMessage.from.userName == botConfig.botname
    }
}
