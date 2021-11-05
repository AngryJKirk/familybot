package space.yaroslav.familybot.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.randomBoolean
import space.yaroslav.familybot.common.extensions.randomInt
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.FunctionId
import space.yaroslav.familybot.models.router.Priority
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.RageMode
import space.yaroslav.familybot.services.settings.TalkingDensity
import space.yaroslav.familybot.services.talking.TalkingService

@Component
class TalkingExecutor(
    private val talkingService: TalkingService,
    private val easyKeyValueService: EasyKeyValueService
) : Executor, Configurable {

    override fun getFunctionId(context: ExecutorContext): FunctionId {
        return if (isRageModeEnabled(context)) {
            FunctionId.RAGE
        } else {
            FunctionId.CHATTING
        }
    }

    override fun priority(context: ExecutorContext): Priority {
        return if (isRageModeEnabled(context)) {
            Priority.HIGH
        } else {
            Priority.RANDOM
        }
    }

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val rageModEnabled = isRageModeEnabled(context)
        if (shouldReply(rageModEnabled, context)) {

            return {
                val messageText = talkingService.getReplyToUser(context)
                    .let { message -> if (rageModEnabled) rageModeFormat(message) else message }
                val delay = if (rageModEnabled.not()) {
                    1000 to 2000
                } else {
                    100 to 500
                }
                it.send(
                    context,
                    messageText,
                    replyToUpdate = true,
                    shouldTypeBeforeSend = true,
                    typeDelay = delay
                )
                if (rageModEnabled) {
                    decrementRageModeMessagesAmount(context)
                }
            }
        } else {
            return {}
        }
    }

    override fun canExecute(context: ExecutorContext): Boolean {
        return isRageModeEnabled(context)
    }

    private fun isRageModeEnabled(context: ExecutorContext): Boolean {
        return easyKeyValueService.get(RageMode, context.chatKey, defaultValue = 0) > 0
    }

    private fun decrementRageModeMessagesAmount(context: ExecutorContext) {
        easyKeyValueService.decrement(RageMode, context.chatKey)
    }

    private fun shouldReply(rageModEnabled: Boolean, context: ExecutorContext): Boolean {
        if (rageModEnabled) {
            return true
        }
        val density = getTalkingDensity(context)
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

    private fun getTalkingDensity(context: ExecutorContext): Long {
        return easyKeyValueService.get(TalkingDensity, context.chatKey, 7)
    }
}
