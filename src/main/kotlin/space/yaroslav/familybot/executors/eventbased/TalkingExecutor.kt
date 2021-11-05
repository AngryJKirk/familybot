package space.yaroslav.familybot.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.common.extensions.randomBoolean
import space.yaroslav.familybot.common.extensions.randomInt
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.toChat
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.FunctionId
import space.yaroslav.familybot.models.router.Priority
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.RageMode
import space.yaroslav.familybot.services.settings.TalkingDensity
import space.yaroslav.familybot.services.talking.TalkingService

@Component
class TalkingExecutor(
    private val talkingService: TalkingService,
    private val easyKeyValueService: EasyKeyValueService
) : Executor, Configurable {

    override fun getFunctionId(executorContext: ExecutorContext): FunctionId {
        return if (isRageModeEnabled(executorContext.chat)) {
            FunctionId.RAGE
        } else {
            FunctionId.CHATTING
        }
    }

    override fun priority(executorContext: ExecutorContext): Priority {
        return if (isRageModeEnabled(executorContext.chat)) {
            Priority.HIGH
        } else {
            Priority.RANDOM
        }
    }

    override fun execute(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {
        val chat = executorContext.chat
        val rageModEnabled = isRageModeEnabled(chat)
        if (shouldReply(rageModEnabled, chat)) {

            return {
                val messageText = talkingService.getReplyToUser(executorContext)
                    .let { message -> if (rageModEnabled) rageModeFormat(message) else message }
                val delay = if (rageModEnabled.not()) {
                    1000 to 2000
                } else {
                    100 to 500
                }
                it.send(
                    executorContext,
                    messageText,
                    replyToUpdate = true,
                    shouldTypeBeforeSend = true,
                    typeDelay = delay
                )
                if (rageModEnabled) {
                    decrementRageModeMessagesAmount(chat)
                }
            }
        } else {
            return {}
        }
    }

    override fun canExecute(executorContext: ExecutorContext): Boolean {
        return isRageModeEnabled(executorContext.message.chat.toChat())
    }

    private fun isRageModeEnabled(chat: Chat): Boolean {
        return easyKeyValueService.get(RageMode, chat.key(), defaultValue = 0) > 0
    }

    private fun decrementRageModeMessagesAmount(chat: Chat) {
        easyKeyValueService.decrement(RageMode, chat.key())
    }

    private fun shouldReply(rageModEnabled: Boolean, chat: Chat): Boolean {
        if (rageModEnabled) {
            return true
        }
        val density = getTalkingDensity(chat)
        return if (density == 0L) {
            true
        } else {
            randomBoolean(density)
        }
    }

    private fun rageModeFormat(string: String): String {
        var message = string
        if (message.endsWith(" ")) {
            message = message.dropLast(1)
        }
        return message.uppercase() + "!".repeat(randomInt(2, 5))
    }

    private fun getTalkingDensity(chat: Chat): Long {
        return easyKeyValueService.get(TalkingDensity, chat.key(), 7)
    }
}
