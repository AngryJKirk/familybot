package space.yaroslav.familybot.route.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.utils.random
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.ChatLogRepository
import space.yaroslav.familybot.repos.ifaces.RagemodeRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.route.models.Priority
import java.util.concurrent.ThreadLocalRandom

@Component
class KeyWordExecutor(
    val keyset: ChatLogRepository,
    val configRepository: RagemodeRepository
) : Executor, Configurable {

    override fun getFunctionId(): FunctionId {
        return FunctionId.CHATTING
    }

    override fun priority(update: Update): Priority {
        return if (configRepository.isEnabled(update.toChat())) {
            Priority.HIGH
        } else {
            Priority.RANDOM
        }
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val chat = update.toChat()
        val rageModEnabled = configRepository.isEnabled(chat)
        if (rageModEnabled || ThreadLocalRandom.current().nextInt(0, 5) == 0) {
            val string = keyset.get(update.toUser()).takeIf { it.size > 300 }?.random()
                ?: getSmallMessage(keyset.getAll())
            val message = if (rageModEnabled) {
                rageModeFormat(string)
            } else {
                string
            }
            return {
                it.execute(
                    SendMessage(chat.id, message)
                        .setReplyToMessageId(update.message.messageId)
                )
                configRepository.decrement(chat)
            }
        } else {
            return {}
        }
    }

    override fun canExecute(message: Message): Boolean {
        return configRepository.isEnabled(message.chat.toChat())
    }

    private fun rageModeFormat(string: String): String {
        var message = string
        if (message.endsWith(" ")) {
            message = message.dropLast(1)
        }
        return message.toUpperCase() + "!!!!"
    }

    private fun getSmallMessage(messages: List<String>): String {
        var message: String?
        do {
            message = messages.random()
        } while (message!!.split(" ").size > 5)
        return message
    }
}