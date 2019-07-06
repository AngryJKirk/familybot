package space.yaroslav.familybot.route.executors.eventbased.askworld

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.utils.bold
import space.yaroslav.familybot.common.utils.italic
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
        val questions = askWorldRepository
            .getQuestionsFromDate()
            .filterNot { it.chat.id == update.message.chatId }
            .filterNot { askWorldRepository.isQuestionDelivered(it, chat) }

        return { sender ->
            questions
                .forEach {
                    val message = SendMessage(
                        chat.id,
                        "${dictionary.get(Phrase.ASK_WORLD_QUESTION_FROM_CHAT)} ${it.chat.name.bold()}: ${it.message.italic()}"
                    )
                        .enableHtml(true)
                    try {
                        val result = sender.execute(message)
                        askWorldRepository.addQuestionDeliver(it.copy(messageId = result.messageId + chat.id), chat)
                    } catch (e: Exception) {
                        log.warn("Could not send question to chat", e)
                    }

                }
        }
    }

    override fun canExecute(message: Message): Boolean {
        return askWorldRepository
            .getQuestionsFromDate()
            .filterNot { it.chat.id == message.chat.id }
            .filterNot { askWorldRepository.isQuestionDelivered(it, message.chat.toChat()) }
            .isNotEmpty()
    }

    override fun priority(update: Update): Priority {
        return Priority.LOW
    }
}
