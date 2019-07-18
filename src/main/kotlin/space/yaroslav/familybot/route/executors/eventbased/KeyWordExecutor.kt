package space.yaroslav.familybot.route.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.randomNotNull
import space.yaroslav.familybot.common.utils.send
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

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val chat = update.toChat()
        val rageModEnabled = configRepository.isEnabled(chat)
        if (shouldReply(rageModEnabled)) {
            val messages = keyset.get(update.toUser()).takeIf { it.size > 300 }
                ?: keyset.getAll().filter { it.split(" ").size <= 10 }

            val messageText = messages
                .let(this::cleanMessages)
                .toList()
                .randomNotNull()
                .let { if (rageModEnabled) rageModeFormat(it) else it }

            return {
                it.send(update, messageText, replyToUpdate = true)
                configRepository.decrement(chat)
            }
        } else {
            return {}
        }
    }

    override fun canExecute(message: Message): Boolean {
        return configRepository.isEnabled(message.chat.toChat())
    }

    private fun shouldReply(rageModEnabled: Boolean) =
        rageModEnabled || ThreadLocalRandom.current().nextInt(0, 7) == 0

    private fun rageModeFormat(string: String): String {
        var message = string
        if (message.endsWith(" ")) {
            message = message.dropLast(1)
        }
        return message.toUpperCase() + "!!!!"
    }

    private fun cleanMessages(messages: List<String>): Sequence<String> {
        return messages
            .asSequence()
            .filterNot { it.length > 600 }
            .filterNot { it.contains("http", ignoreCase = true) }
    }
}
