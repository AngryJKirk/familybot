package space.yaroslav.familybot.route.executors.eventbased.askworld

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.repos.ifaces.AskWorldRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.route.models.Priority

@Component
class AskWorldSendQuestionExecutor(val askWorldRepository: AskWorldRepository) : Executor, Configurable {
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
                        val message = SendMessage(chat.id, "Вопрос из чата ${it.chat.name}: ${it.message}")
                        val result = sender.execute(message)
                        askWorldRepository.addQuestionDeliver(it.copy(messageId = result.messageId), chat)
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