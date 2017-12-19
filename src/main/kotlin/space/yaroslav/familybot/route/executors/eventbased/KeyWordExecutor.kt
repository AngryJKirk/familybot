package space.yaroslav.familybot.route.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.random
import space.yaroslav.familybot.common.toUser
import space.yaroslav.familybot.repos.ifaces.ChatLogRepository
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.Priority
import java.util.concurrent.ThreadLocalRandom

@Component
class KeyWordExecutor(val keyset: ChatLogRepository) : Executor {
    override fun priority(): Priority {
        return Priority.LOW
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        if (ThreadLocalRandom.current().nextInt(0, 5) == 3) {
            val get = keyset.get(update.message.from.toUser(telegramChat = update.message.chat))
            if (get.size < 100) return {}
            return {
                it.execute(SendMessage(update.message.chatId, get.random())
                        .setReplyToMessageId(update.message.messageId))
            }
        } else {
            return {}
        }

    }

    override fun canExecute(message: Message): Boolean {
        return false
    }
}