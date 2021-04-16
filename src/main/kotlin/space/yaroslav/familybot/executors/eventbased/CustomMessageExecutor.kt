package space.yaroslav.familybot.executors.eventbased

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.Priority
import space.yaroslav.familybot.repos.CustomMessageDeliveryRepository

@Component
class CustomMessageExecutor(private val repository: CustomMessageDeliveryRepository) : Executor {
    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        return { sender ->
            val chat = update.toChat()
            if (repository.hasNewMessages(chat)) {
                repository.getNewMessages(chat)
                    .forEach { message ->
                        coroutineScope {
                            launch {
                                delay(2000)
                                sender.execute(SendMessage(message.chat.idString, message.message))
                                repository.markAsDelivered(message)
                            }
                        }
                    }
            }
        }
    }

    override fun canExecute(message: Message): Boolean {
        return repository.hasNewMessages(message.chat.toChat())
    }

    override fun priority(update: Update): Priority {
        return Priority.LOW
    }
}
