package dev.storozhenko.familybot.feature.talking.executors

import dev.storozhenko.familybot.common.extensions.randomBoolean
import dev.storozhenko.familybot.common.extensions.randomInt
import dev.storozhenko.familybot.common.extensions.sendDeferred
import dev.storozhenko.familybot.core.executors.Configurable
import dev.storozhenko.familybot.core.executors.Executor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority
import dev.storozhenko.familybot.feature.settings.models.FunctionId
import dev.storozhenko.familybot.feature.settings.models.RageMode
import dev.storozhenko.familybot.feature.settings.models.TalkingDensity
import dev.storozhenko.familybot.feature.talking.services.TalkingService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class TalkingExecutor(
    @Qualifier("Picker") private val talkingService: TalkingService,
    private val easyKeyValueService: EasyKeyValueService,
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

    override suspend fun execute(context: ExecutorContext) {
        val rageModEnabled = isRageModeEnabled(context)
        if (!shouldReply(rageModEnabled, context)) {
            return
        }
        coroutineScope {
            val messageText = async {
                talkingService.getReplyToUser(context)
                    .let { message -> if (rageModEnabled) rageModeFormat(message) else message }
            }
            val delay = if (rageModEnabled.not()) {
                1000 to 2000
            } else {
                100 to 500
            }
            context.sender.sendDeferred(
                context,
                messageText,
                replyToUpdate = true,
                shouldTypeBeforeSend = true,
                typeDelay = delay,
                enableHtml = true,
            )
            if (rageModEnabled) {
                decrementRageModeMessagesAmount(context)
            }
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
