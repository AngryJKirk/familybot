package space.yaroslav.familybot.executors.eventbased.askworld

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
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.FunctionId
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.models.Priority
import space.yaroslav.familybot.repos.ifaces.AskWorldRepository
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.services.dictionary.Dictionary

@Component
class AskWorldSendQuestionExecutor(
    private val askWorldRepository: AskWorldRepository,
    private val dictionary: Dictionary,
    private val commonRepository: CommonRepository
) : Executor, Configurable {

    private val log = LoggerFactory.getLogger(AskWorldSendQuestionExecutor::class.java)

    override fun getFunctionId(): FunctionId {
        return FunctionId.ASK_WORLD
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
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
                        }.onFailure { e ->
                            log.warn("Could not send question $question to chat $chat", e)
                            val questionWithId = question.copy(messageId = -1)
                            askWorldRepository.addQuestionDeliver(questionWithId, chat)
                        }
                    }
                }
        }
    }

    private fun formatQuestion(question: AskWorldQuestion): String {
        val messagePrefix = dictionary.get(Phrase.ASK_WORLD_QUESTION_FROM_CHAT)
        val boldChatName = question.chat.name.boldNullable()
        val italicQuestion = question.message.replace("<", "").replace(">", "").italic()
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
