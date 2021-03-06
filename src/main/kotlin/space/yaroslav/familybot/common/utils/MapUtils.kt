package space.yaroslav.familybot.common.utils

import org.telegram.telegrambots.meta.api.objects.EntityType
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.MessageEntity
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.models.Command
import space.yaroslav.familybot.services.settings.SettingsKey
import space.yaroslav.familybot.telegram.FamilyBot
import org.telegram.telegrambots.meta.api.objects.Chat as TelegramChat
import org.telegram.telegrambots.meta.api.objects.User as TelegramUser

fun TelegramChat.toChat(): Chat = Chat(this.id, this.title)

fun TelegramUser.toUser(chat: Chat? = null, telegramChat: TelegramChat? = null): User {
    val internalChat = telegramChat?.toChat()
        ?: chat
        ?: throw FamilyBot.InternalException("Should be some chat to map user to internal model")
    val formattedName = listOfNotNull(this.firstName, this.lastName).joinToString(separator = " ")
    return User(this.id.toLong(), internalChat, formattedName, this.userName)
}

fun Update.toChat(): Chat {
    return when {
        hasMessage() -> Chat(message.chat.id, message.chat.title)
        hasEditedMessage() -> Chat(editedMessage.chat.id, editedMessage.chat.title)
        else -> Chat(this.callbackQuery.message.chat.id, this.callbackQuery.message.chat.title)
    }
}

fun Update.chatId(): Long {
    return when {
        hasMessage() -> message.chat.id
        hasEditedMessage() -> editedMessage.chat.id
        else -> callbackQuery.message.chat.id
    }
}

fun Update.chatIdString(): String {
    return this.chatId().toString()
}

fun Update.toUser(): User {
    val user = this.from()
    val formattedName = (user.firstName.let { "$it " }) + (user.lastName ?: "")
    return User(user.id.toLong(), this.toChat(), formattedName, user.userName)
}

fun Update.from(): TelegramUser {
    return when {
        hasMessage() -> message.from
        hasEditedMessage() -> editedMessage.from
        hasCallbackQuery() -> callbackQuery.from
        hasPollAnswer() -> pollAnswer.user
        else -> throw FamilyBot.InternalException("Cant process $this")
    }
}

fun Message.getCommand(botName: () -> String): Command? {
    val botNameValue = botName()
    val textCommand = this
        .entities
        ?.asSequence()
        ?.filter { entity -> entity.offset == 0 }
        ?.filter { entity -> entity.type == EntityType.BOTCOMMAND }
        ?.map(MessageEntity::getText)
        ?.map { command -> command.split("@") }
        ?.filter { commandParts -> commandParts.size == 1 || commandParts[1] == botNameValue }
        ?.map { commandParts -> commandParts.first() }
        ?.firstOrNull() ?: return null

    return Command.values().find { command -> command.command == textCommand }
}

fun Update.key(): SettingsKey {
    return SettingsKey(this.chatId(), this.toUser().id)
}

fun User.key(): SettingsKey {
    return SettingsKey(userId = this.id)
}

fun Chat.key(): SettingsKey {
    return SettingsKey(chatId = this.id)
}
