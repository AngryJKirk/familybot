package space.yaroslav.familybot.route.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.random
import space.yaroslav.familybot.common.utils.randomNotNull
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.ChatLogRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.route.models.Priority
import space.yaroslav.familybot.telegram.BotConfig

@Component
class ReplyToUserExecutor(val keyset: ChatLogRepository, val botConfig: BotConfig) : Executor, Configurable {
    override fun getFunctionId(): FunctionId {
        return FunctionId.CHATTING
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val string = getRandomUserMessage(update) ?: getSmallRandomMessage(keyset.getAll())
        return { it.send(update, string, replyToUpdate = true) }
    }

    override fun canExecute(message: Message): Boolean {
        return message.isReply && message.replyToMessage.from.userName == botConfig.botname
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
}
