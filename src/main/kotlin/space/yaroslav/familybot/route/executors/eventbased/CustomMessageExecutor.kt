package space.yaroslav.familybot.route.executors.eventbased

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.repos.ifaces.CustomMessageDeliveryRepository
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.Priority

@Component
class CustomMessageExecutor(val repository: CustomMessageDeliveryRepository) : Executor {
    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        return { sender ->
            val chat = update.toChat()
            if (repository.hasNewMessages(chat)) {
                repository.getNewMessages(chat)
                    .forEach { message ->
                        GlobalScope.launch {
                            delay(2000)
                            sender.execute(SendMessage(message.chat.id, message.message))
                            repository.markAsDelivered(message)
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
