package space.yaroslav.familybot.infrastructure

import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.MessageEntity
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.models.telegram.stickers.Sticker
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker as TelegramSticker

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
    return User().apply {
        id = userId
        userName = "user$userId"
        firstName = "test user"
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
