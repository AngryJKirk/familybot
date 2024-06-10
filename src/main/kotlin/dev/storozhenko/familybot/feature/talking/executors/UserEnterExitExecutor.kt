package dev.storozhenko.familybot.feature.talking.executors

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.Configurable
import dev.storozhenko.familybot.core.executors.Executor
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority
import dev.storozhenko.familybot.feature.settings.models.FunctionId
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.message.Message

@Component
class UserEnterExitExecutor(private val botConfig: BotConfig) :
    Executor,
    Configurable {
    override suspend fun execute(context: ExecutorContext) {
        val message = context.message
        val phrase = when {
            isUserLeft(message) -> Phrase.USER_LEAVING_CHAT
            isNewChat(message) -> Phrase.BOT_WELCOME_MESSAGE
            else -> Phrase.USER_ENTERING_CHAT
        }
        context.client.send(
            context,
            context.phrase(phrase),
            replyToUpdate = true,
            shouldTypeBeforeSend = true,
        )
    }

    override fun canExecute(context: ExecutorContext) =
        isUserLeft(context.message) || isUserEntered(context.message)

    override fun priority(context: ExecutorContext) = Priority.LOW

    private fun isUserLeft(message: Message) = message.leftChatMember != null
    private fun isUserEntered(message: Message) = message.newChatMembers?.isNotEmpty() ?: false
    private fun isNewChat(message: Message) =
        message.newChatMembers.any { it.isBot && it.userName == botConfig.botName }

    override fun getFunctionId(context: ExecutorContext) = FunctionId.GREETINGS
}
