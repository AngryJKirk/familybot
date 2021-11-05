package space.yaroslav.familybot.models.router

import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.models.telegram.User
import space.yaroslav.familybot.services.settings.ChatEasyKey
import space.yaroslav.familybot.services.settings.UserAndChatEasyKey
import space.yaroslav.familybot.services.settings.UserEasyKey
import space.yaroslav.familybot.services.talking.Dictionary

class ExecutorContext(
    val update: Update,
    val message: Message,
    val command: Command?,
    val isFromDeveloper: Boolean,
    val chat: Chat,
    val user: User,
    val userAndChatKey: UserAndChatEasyKey,
    val userKey: UserEasyKey,
    val chatKey: ChatEasyKey,
    private val dictionary: Dictionary
) {
    fun phrase(phrase: Phrase) = dictionary.get(phrase, chatKey)
}
