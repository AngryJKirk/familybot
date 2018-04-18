package space.yaroslav.familybot.route.executors.eventbased.askworld

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.AskWorldReply
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.AskWorldRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.route.models.Priority
import space.yaroslav.familybot.telegram.BotConfig
import java.time.Instant

@Component
class AskWorldRecieveReplyExecutor(val askWorldRepository: AskWorldRepository,
                                   val botConfig: BotConfig) : Executor, Configurable {
    override fun getFunctionId(): FunctionId {
        return FunctionId.ASK_WORLD
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val reply = update.message.text
        val chat = update.toChat()
        val user = update.toUser()
        val question = askWorldRepository.findQuestionByMessageId(update.message.replyToMessage.messageId, chat)

        if (askWorldRepository.isReplied(question, chat, user)) {
            return { it.execute(SendMessage(chat.id, "Отвечать можно только раз").setReplyToMessageId(update.message.messageId)) }
        }
        askWorldRepository.addReply(AskWorldReply(null,
                question.id!!,
                reply,
                user,
                chat,
                Instant.now()
        ))
        return { it.execute(SendMessage(update.message.chatId, "Принято")) }
    }

    override fun canExecute(message: Message): Boolean {
        return message.replyToMessage
                ?.takeIf { it.from.bot && it.from.userName == botConfig.botname }
                ?.text
                ?.startsWith("Вопрос из чата ") ?: false
    }

    override fun priority(update: Update): Priority {
        return Priority.LOW
    }
}