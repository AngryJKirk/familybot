package space.yaroslav.familybot.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.FunctionId
import space.yaroslav.familybot.models.router.Priority
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.telegram.BotConfig

@Component
class UserListExecutor(private val dictionary: Dictionary, private val botConfig: BotConfig) : Executor, Configurable {
    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val message = update.message
        val phrase = when {
            isUserLeft(message) -> Phrase.USER_LEAVING_CHAT
            isSucharaEntered(message) -> Phrase.SUCHARA_HELLO_MESSAGE
            else -> Phrase.USER_ENTERING_CHAT
        }
        return {
            it.send(
                update, dictionary.get(phrase, update),
                replyToUpdate = true,
                shouldTypeBeforeSend = true
            )
        }
    }

    override fun canExecute(message: Message) = isUserLeft(message) || isUserEntered(message)

    override fun priority(update: Update) = Priority.LOW

    private fun isUserLeft(message: Message) = message.leftChatMember != null
    private fun isUserEntered(message: Message) = message.newChatMembers?.isNotEmpty() ?: false
    private fun isSucharaEntered(message: Message) =
        message.newChatMembers.any { it.isBot && it.userName == botConfig.botName }

    override fun getFunctionId() = FunctionId.GREETINGS
}
