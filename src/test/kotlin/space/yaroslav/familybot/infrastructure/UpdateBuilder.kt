package space.yaroslav.familybot.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.MessageEntity
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import space.yaroslav.familybot.route.models.Command
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

interface TestModelBuilder<T> {
    companion object {
        val objectMapper = ObjectMapper()
    }

    val data: MutableMap<String, Any>

    fun type(): Class<T>

    fun build(): T = objectMapper.readValue(objectMapper.writeValueAsString(data), type())
}

class UpdateBuilder(override val data: MutableMap<String, Any> = HashMap()) : TestModelBuilder<Update> {

    init {
        data.putAll(
            mapOf(
                "update_id" to randomInt(),
                "date" to Instant.now().epochSecond
            )
        )
    }

    fun message(build: MessageBuilder.() -> MessageBuilder): UpdateBuilder {
        val messageBuilder = build(MessageBuilder())
        data["message"] = messageBuilder.data
        data.remove("edited_message")
        return this
    }

    fun withEditedMessage(build: MessageBuilder.() -> MessageBuilder): UpdateBuilder {
        val messageBuilder = build(MessageBuilder())
        data["edited_message"] = messageBuilder.data
        data.remove("message")
        return this
    }

    fun simpleTextMessageFromUser(text: String): Update {
        return message {
            text { text }
            chat { ChatBuilder() }
            from { UserBuilder() }
        }.build()
    }

    fun simpleCommandFromUser(command: Command, prefix: String? = null, postfix: String? = null): Update {
        return message {
            command(length = command.command.length, offset = prefix?.length ?: 0)
            text { (prefix ?: "") + command.command + (postfix ?: "") }
            chat { ChatBuilder() }
            from { UserBuilder() }
        }.build()
    }

    override fun type() = Update::class.java
}

class MessageBuilder(override val data: MutableMap<String, Any> = HashMap()) : TestModelBuilder<Message> {

    init {
        data.putAll(
            mapOf(
                "message_id" to randomInt(),
                "date" to Instant.now().epochSecond,
                "text" to UUID.randomUUID().toString()
            )
        )
    }

    fun text(text: () -> String): MessageBuilder {
        data["text"] = text()
        return this
    }

    fun chat(chat: () -> ChatBuilder): MessageBuilder {
        data["chat"] = chat().data
        return this
    }

    fun command(offset: Int = 0, length: Int): MessageBuilder {
        data["entities"] = listOf(MessageEntityBuilder(offset = offset, length = length).data)
        return this
    }

    fun from(from: () -> UserBuilder): MessageBuilder {
        data["from"] = from().data
        return this
    }

    fun to(messageTo: MessageBuilder.() -> MessageBuilder): MessageBuilder {
        data["reply_to_message"] = messageTo(MessageBuilder()).data
        return this
    }

    override fun type() = Message::class.java
}

class ChatBuilder(override val data: MutableMap<String, Any> = HashMap()) : TestModelBuilder<Chat> {

    init {
        val chatId = randomIntFrom1to3() * 10
        data.putAll(
            mapOf(
                "id" to chatId,
                "title" to "Test chat #$chatId",
                "type" to "supergroup"
            )
        )
    }

    fun becomeUser(username: String): ChatBuilder {
        data.putAll(
            mapOf(
                "type" to "private",
                "username" to username
            )
        )
        return this
    }

    override fun type() = Chat::class.java
}

class UserBuilder(override val data: MutableMap<String, Any> = HashMap()) : TestModelBuilder<User> {
    override fun type() = User::class.java

    init {
        val userId = randomIntFrom1to3()
        data.putAll(
            mapOf(
                "id" to userId,
                "username" to "user$userId",
                "first_name" to "Test user",
                "last_name" to "#$userId"
            )
        )
    }

    fun username(text: () -> String): UserBuilder {
        data["username"] = text()
        return this
    }

    fun toBot(name: String): UserBuilder {
        data.putAll(
            mapOf(
                "username" to name,
                "is_bot" to true
            )
        )
        return this
    }
}

class MessageEntityBuilder(
    override val data: MutableMap<String, Any> = HashMap(),
    offset: Int,
    length: Int
) : TestModelBuilder<MessageEntity> {

    init {
        data.putAll(
            mapOf(
                "type" to "bot_command",
                "offset" to offset,
                "length" to length
            )
        )
    }

    override fun type(): Class<MessageEntity> = MessageEntity::class.java
}

private fun randomInt() = ThreadLocalRandom.current().nextInt()
private fun randomIntFrom1to3() = ThreadLocalRandom.current().nextInt(1, 3)
