package space.yaroslav.familybot.infrastructure

import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.MessageEntity
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import space.yaroslav.familybot.common.extensions.context
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.models.telegram.stickers.Sticker
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.UkrainianLanguage
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.services.talking.DictionaryReader
import space.yaroslav.familybot.telegram.BotConfig
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker as TelegramSticker

private val keyValueService = mock<EasyKeyValueService> {
    on { get(eq(UkrainianLanguage), any()) }.thenReturn(false)
}
private val dictionary = Dictionary(keyValueService, DictionaryReader())

private val botConfig = BotConfig(
    botToken = "123",
    botName = "IntegrationTests",
    developer = "IntegrationTestsDeveloper",
    developerId = "123",
    botNameAliases = listOf("IntegrationTests"),
    yandexKey = null,
    paymentToken = null,
    testEnvironment = true
)

fun createSimpleContext(text: String? = null): ExecutorContext {

    return update(text).context(botConfig, dictionary)
}

fun Update.createContext() = this.context(botConfig, dictionary)

fun createSimpleCommandContext(command: Command, prefix: String? = null, postfix: String? = null): ExecutorContext {
    return createSimpleCommand(command, prefix, postfix).context(botConfig, dictionary)
}

fun createSimpleUpdate(text: String? = null): Update {
    return update(text)
}

fun createSimpleMessage(text: String? = null, chat: Chat = chat()): Message {
    return message(text, chat)
}

fun createSimpleCommand(command: Command, prefix: String? = null, postfix: String? = null): Update {
    val entireMessage = (prefix ?: "") + command.command + (postfix ?: "")
    return update(entireMessage).apply {
        val element = MessageEntity(
            "bot_command",
            prefix?.length ?: 0,
            command.command.length + (postfix?.length ?: 0)
        )
            .apply { text = command.command }
        this.message.entities = mutableListOf()
        this.message.entities.add(element)
    }
}

fun createSimpleUser(isBot: Boolean = false, botName: String? = null): User {
    val user = user()
    if (isBot) {
        user.isBot = true
        user.userName = botName
    }
    return user
}

fun singleStickerContext(sticker: Sticker): ExecutorContext {
    return update()
        .apply {
            message.sticker = TelegramSticker().apply {
                emoji = sticker.stickerEmoji
                setName = sticker.pack.packName
                fileId = randomString()
            }
        }.context(botConfig, dictionary)
}

private fun update(text: String? = null): Update {
    return Update().apply {
        message = message(text, chat())
    }
}

private fun message(text: String?, chat: Chat): Message {
    return Message().apply {
        if (text != null) {
            this.text = text
        }
        this.chat = chat
        from = user()
        messageId = randomInt()
    }
}

private fun user(): User {
    val userId = randomUserId()
    return User().apply {
        id = userId
        userName = "user$userId"
        firstName = "Test user"
        lastName = "#$userId"
        isBot = false
    }
}

private fun chat(): Chat {
    val chatId = randomChatId()
    return Chat().apply {
        id = chatId
        title = "Test chat #$chatId"
        type = "supergroup"
    }
}
