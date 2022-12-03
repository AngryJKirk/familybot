package dev.storozhenko.familybot.executors.eventbased

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.executors.Configurable
import dev.storozhenko.familybot.executors.Executor
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.router.FunctionId
import dev.storozhenko.familybot.models.router.Priority
import dev.storozhenko.familybot.telegram.BotConfig
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class UserEnterExitExecutor(private val botConfig: BotConfig) :
    Executor,
    Configurable {
    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val message = context.message
        val phrase = when {
            isUserLeft(message) -> Phrase.USER_LEAVING_CHAT
            isNewChat(message) -> Phrase.BOT_WELCOME_MESSAGE
            else -> Phrase.USER_ENTERING_CHAT
        }
        return {
            it.send(
                context,
                context.phrase(phrase),
                replyToUpdate = true,
                shouldTypeBeforeSend = true
            )
        }
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
