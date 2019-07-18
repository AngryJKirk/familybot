package space.yaroslav.familybot.route.executors.eventbased.askworld

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.AskWorldQuestion
import space.yaroslav.familybot.common.AskWorldReply
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.utils.boldNullable
import space.yaroslav.familybot.common.utils.italic
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.repos.ifaces.AskWorldRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.models.Priority
import space.yaroslav.familybot.route.services.dictionary.Dictionary

@Component
class AskWorldSendReplyExecutor(
    val askWorldRepository: AskWorldRepository,
    val dictionary: Dictionary
) : Executor, Configurable {
    override fun getFunctionId(): FunctionId {
        return FunctionId.ASK_WORLD
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val chat = update.toChat()
        val replyToDeliver = getRepliesToDeliver(chat)
        val question = askWorldRepository.findQuestionByMessageId(extractQuestionMessageId(update, chat), chat)
        return { sender ->
            replyToDeliver.forEach {
                GlobalScope.launch {
                    sender.send(
                        update,
                        formatReplyMessageText(it, cutQuestionTextIfTooLong(question)),
                        enableHtml = true
                    )
                    askWorldRepository.addReplyDeliver(it)
                }
            }
        }
    }

    override fun canExecute(message: Message): Boolean {
        return askWorldRepository
            .getQuestionsFromChat(message.chat.toChat())
            .flatMap(askWorldRepository::getReplies)
            .any { !askWorldRepository.isReplyDelivered(it) }
    }

    override fun priority(update: Update): Priority {
        return Priority.LOW
    }

    private fun formatReplyMessageText(
        askWorldReply: AskWorldReply,
        questionMessage: String
    ): String {
        val replyFromChatPrefix = dictionary.get(Phrase.ASK_WORLD_REPLY_FROM_CHAT)
        val boldChatName = askWorldReply.chat.name.boldNullable()
        val userGeneralName = askWorldReply.user.getGeneralName()
        val italicReplyText = askWorldReply.message.italic()

        return "$replyFromChatPrefix $boldChatName от $userGeneralName " +
            "на вопрсос \"$questionMessage\" : $italicReplyText"
    }

    private fun extractQuestionMessageId(
        update: Update,
        chat: Chat
    ) = update.message.replyToMessage.messageId + chat.id

    private fun getRepliesToDeliver(chat: Chat): List<AskWorldReply> {
        return askWorldRepository
            .getQuestionsFromChat(chat)
            .flatMap(askWorldRepository::getReplies)
            .filterNot(askWorldRepository::isReplyDelivered)
    }

    private fun cutQuestionTextIfTooLong(question: AskWorldQuestion) =
        question.message.takeIf { it.length < 100 } ?: question.message.take(100) + "..."
}
