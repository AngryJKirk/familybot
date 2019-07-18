package space.yaroslav.familybot.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import org.telegram.telegrambots.meta.api.objects.Update
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

class UpdateBuilder(private val data: MutableMap<String, Any> = HashMap()) {

    init {
        data.putAll(
            mapOf(
                "update_id" to randomInt(),
                "date" to Instant.now().epochSecond
            )
        )
    }

    private val objectMapper = ObjectMapper()

    fun addMessage(messageBuilder: MessageBuilder): UpdateBuilder {
        data["message"] = messageBuilder.data
        data.remove("edited_message")
        return this
    }

    fun addEditedMessage(messageBuilder: MessageBuilder): UpdateBuilder {
        data["edited_message"] = messageBuilder.data
        data.remove("message")
        return this
    }

    fun simpleTextMessageFromUser(text: String): Update {
        return addMessage(
            MessageBuilder()
                .addText(text)
                .addChat(ChatBuilder())
                .addFrom(UserBuilder())
        ).build()
    }

    fun build(): Update = objectMapper.readValue(objectMapper.writeValueAsString(data), Update::class.java)
}

class MessageBuilder(val data: MutableMap<String, Any> = HashMap()) {
    init {
        data.putAll(
            mapOf(
                "message_id" to randomInt(),
                "date" to Instant.now().epochSecond,
                "text" to UUID.randomUUID().toString()
            )
        )
    }

    fun addText(text: String): MessageBuilder {
        data["text"] = text
        return this
    }

    fun addChat(chatBuilder: ChatBuilder): MessageBuilder {
        data["chat"] = chatBuilder.data
        return this
    }

    fun addFrom(userBuilder: UserBuilder): MessageBuilder {
        data["from"] = userBuilder.data
        return this
    }

    fun addRepledMessage(messageBuilder: MessageBuilder): MessageBuilder {
        data["reply_to_message"] = messageBuilder.data
        return this
    }
}

class ChatBuilder(val data: MutableMap<String, Any> = HashMap()) {
    init {
        val chatId = randomIntFrom1to3()
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
}

class UserBuilder(val data: MutableMap<String, Any> = HashMap()) {
    init {
        val userId = randomIntFrom1to3()
        data.putAll(
            mapOf(
                "id" to userId,
                "username" to "user$userId"
            )
        )
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

private fun randomInt() = ThreadLocalRandom.current().nextInt()
private fun randomIntFrom1to3() = ThreadLocalRandom.current().nextInt(1, 3)

