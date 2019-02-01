package space.yaroslav.familybot.route.executors.eventbased

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.repos.ifaces.CustomMessageDeliveryRepository
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.Priority

@Component
class CustomMessageExecutor(val repository: CustomMessageDeliveryRepository) : Executor {
    override fun execute(update: Update): (AbsSender) -> Unit {
        return { sender ->
            val chat = update.toChat()
            val newMessages = GlobalScope.async { repository.getNewMessages(chat) }
            if (repository.hasNewMessages(chat)) {
                GlobalScope.launch {
                    newMessages
                        .await()
                        .forEach {
                            Thread.sleep(2000)
                            sender.execute(SendMessage(it.chat.id, it.message))
                            repository.markAsDelivered(it)
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
