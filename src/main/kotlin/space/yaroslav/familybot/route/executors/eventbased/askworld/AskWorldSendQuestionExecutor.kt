package space.yaroslav.familybot.route.executors.eventbased.askworld

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.AskWorldQuestion
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
class AskWorldSendQuestionExecutor(
    val askWorldRepository: AskWorldRepository,
    val dictionary: Dictionary
) : Executor, Configurable {

    private val log = LoggerFactory.getLogger(AskWorldSendQuestionExecutor::class.java)

    override fun getFunctionId(): FunctionId {
        return FunctionId.ASK_WORLD
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val chat = update.toChat()
        val questions = getQuestionList(update.message)

        return { sender ->
            questions
                .forEach { question ->
                    GlobalScope.launch {
                        runCatching {
                            val result = sender.send(update, formatQuestion(question), enableHtml = true)
                            val questionWithId = assignQuestionNewId(question, result)
                            askWorldRepository.addQuestionDeliver(questionWithId, chat)
                        }.onFailure { e -> log.warn("Could not send question to chat", e) }
                    }
                }
        }
    }

    private fun formatQuestion(question: AskWorldQuestion): String {
        val messagePrefix = dictionary.get(Phrase.ASK_WORLD_QUESTION_FROM_CHAT)
        val boldChatName = question.chat.name.boldNullable()
        val italicQuestion = question.message.italic()
        return "$messagePrefix $boldChatName: $italicQuestion"
    }

    private fun assignQuestionNewId(
        question: AskWorldQuestion,
        result: Message
    ) = question.copy(messageId = result.messageId + result.chat.id)

    override fun canExecute(message: Message): Boolean = getQuestionList(message).isNotEmpty()

    override fun priority(update: Update): Priority {
        return Priority.LOW
    }

    private fun getQuestionList(
        message: Message
    ): List<AskWorldQuestion> {
        return askWorldRepository
            .getQuestionsFromDate()
            .filterNot { it.chat.id == message.chat.id }
            .filterNot { askWorldRepository.isQuestionDelivered(it, message.chat.toChat()) }
    }
}
