package space.yaroslav.familybot.route.executors.eventbased

import java.util.concurrent.ThreadLocalRandom
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.utils.randomNotNull
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.ChatLogRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.route.models.Priority
import space.yaroslav.familybot.route.services.state.RageModeState
import space.yaroslav.familybot.route.services.state.StateService

@Component
class TalkingExecutor(
    private val keyset: ChatLogRepository,
    private val stateService: StateService
) : Executor, Configurable {

    override fun getFunctionId(): FunctionId {
        return FunctionId.CHATTING
    }

    override fun priority(update: Update): Priority {
        return if (getConfig(update.toChat()) != null) {
            Priority.HIGH
        } else {
            Priority.RANDOM
        }
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val chat = update.toChat()
        val rageModeConfiguration = getConfig(chat)
        val rageModEnabled = rageModeConfiguration != null
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
                rageModeConfiguration?.decrement()
            }
        } else {
            return {}
        }
    }

    override fun canExecute(message: Message): Boolean {
        return getConfig(message.chat.toChat()) != null
    }

    private fun getConfig(chat: Chat) =
        stateService.getStateForChat(chat.id, RageModeState::class)

    private fun shouldReply(rageModEnabled: Boolean): Boolean {
        return rageModEnabled || ThreadLocalRandom.current().nextInt(0, 1) == 0
    }

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
