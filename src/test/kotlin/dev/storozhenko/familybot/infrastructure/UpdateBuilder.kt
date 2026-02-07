package dev.storozhenko.familybot.infrastructure

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.context
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.models.telegram.stickers.Sticker
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.UkrainianLanguage
import dev.storozhenko.familybot.feature.shop.model.ShopPayload
import dev.storozhenko.familybot.feature.talking.services.Dictionary
import dev.storozhenko.familybot.feature.talking.services.DictionaryReader
import dev.storozhenko.familybot.suits.ExecutorTest
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.telegram.telegrambots.meta.api.objects.MessageEntity
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chat.Chat
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.generics.TelegramClient
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker as TelegramSticker

private val keyValueService = mock<EasyKeyValueService> {
    on { get(eq(UkrainianLanguage), any()) }.thenReturn(false)
}
val dictionary = Dictionary(keyValueService, DictionaryReader())

val botConfig = BotConfig(
    botToken = "123",
    botName = "IntegrationTests",
    developerId = 123L,
    botNameAliases = listOf("IntegrationTests"),
    yandexKey = null,
    testEnvironment = true,
    ytdlLocation = null,
    aiToken = null,
    aiApiUrl = null,
    aiModel = null
)

fun ExecutorTest.createSimpleContext(text: String? = null, custom: Update.() -> Unit = {}): ExecutorContext {
    return update(text).apply(custom).context(botConfig, dictionary, this.client)
}

fun Update.createContext(client: TelegramClient) = this.context(botConfig, dictionary, client)

fun ExecutorTest.createSimpleCommandContext(
    command: Command,
    prefix: String? = null,
    postfix: String? = null,
): ExecutorContext {
    return createSimpleCommand(command, prefix, postfix).context(botConfig, dictionary, this.client)
}

fun createUpdateForPayment(payload: ShopPayload): Update {
    return update().apply {
        message = Message().apply {
            from = createSimpleUser().apply {
                id = payload.userId
            }
            chat = Chat(payload.chatId, randomString())
        }
    }
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
            command.command.length + (postfix?.length ?: 0),
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

fun singleStickerUpdate(sticker: Sticker): Update {
    return update()
        .apply {
            message.sticker = TelegramSticker().apply {
                emoji = sticker.stickerEmoji
                setName = sticker.pack.packName
                fileId = randomString()
            }
        }
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
    return User(userId, "Test user", false).apply {
        userName = "user$userId"
        lastName = "#$userId"
    }
}

private fun chat(): Chat {
    val chatId = randomChatId()
    return Chat(chatId, "supergroup").apply { title = "Test chat #$chatId" }
}
