package space.yaroslav.familybot.executors.eventbased

import java.util.concurrent.ThreadLocalRandom
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.FunctionId
import space.yaroslav.familybot.models.Priority
import space.yaroslav.familybot.services.TalkingService
import space.yaroslav.familybot.services.state.RageModeState
import space.yaroslav.familybot.services.state.StateService

@Component
class TalkingExecutor(
    private val talkingService: TalkingService,
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

            val messageText = talkingService.getReplyToUser(update)
                .let { if (rageModEnabled) rageModeFormat(it) else it }

            return {
                val delay = if (rageModEnabled.not()) {
                    1000L to 2000L
                } else {
                    100L to 500L
                }
                it.send(
                    update,
                    messageText,
                    replyToUpdate = true,
                    shouldTypeBeforeSend = true,
                    typeDelay = delay
                )
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
        return rageModEnabled || ThreadLocalRandom.current().nextInt(0, 7) == 0
    }

    private fun rageModeFormat(string: String): String {
        var message = string
        if (message.endsWith(" ")) {
            message = message.dropLast(1)
        }
        return message.toUpperCase() + "!!!!"
    }
}
