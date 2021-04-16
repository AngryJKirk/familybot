package space.yaroslav.familybot.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.utils.key
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.FunctionId
import space.yaroslav.familybot.models.Priority
import space.yaroslav.familybot.services.TalkingService
import space.yaroslav.familybot.services.settings.EasySettingsService
import space.yaroslav.familybot.services.settings.RageMode
import space.yaroslav.familybot.services.settings.TalkingDencity
import java.util.concurrent.ThreadLocalRandom

@Component
class TalkingExecutor(
    private val talkingService: TalkingService,
    private val easySettingsService: EasySettingsService
) : Executor, Configurable {

    override fun getFunctionId(): FunctionId {
        return FunctionId.CHATTING
    }

    override fun priority(update: Update): Priority {
        return if (isRageModeEnabled(update.toChat())) {
            Priority.HIGH
        } else {
            Priority.RANDOM
        }
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val chat = update.toChat()
        val rageModEnabled = isRageModeEnabled(chat)
        if (shouldReply(rageModEnabled, chat)) {

            return {
                val messageText = talkingService.getReplyToUser(update)
                    .let { message -> if (rageModEnabled) rageModeFormat(message) else message }
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
                if (rageModEnabled) {
                    decrementRageModeMessagesAmount(chat)
                }
            }
        } else {
            return {}
        }
    }

    override fun canExecute(message: Message): Boolean {
        return isRageModeEnabled(message.chat.toChat())
    }

    private fun isRageModeEnabled(chat: Chat): Boolean {
        return easySettingsService.get(RageMode, chat.key(), defaultValue = 0) > 0
    }

    private fun decrementRageModeMessagesAmount(chat: Chat) {
        easySettingsService.decrement(RageMode, chat.key())
    }

    private fun shouldReply(rageModEnabled: Boolean, chat: Chat): Boolean {
        if (rageModEnabled) {
            return true
        }
        val density = getTalkingDensity(chat)
        return if (density == 0L) {
            true
        } else {
            ThreadLocalRandom.current().nextLong(0, density) == 0L
        }
    }

    private fun rageModeFormat(string: String): String {
        var message = string
        if (message.endsWith(" ")) {
            message = message.dropLast(1)
        }
        return message.toUpperCase() + "!!!!"
    }

    private fun getTalkingDensity(chat: Chat): Long {
        return easySettingsService.get(TalkingDencity, chat.key(), 7)
    }
}
