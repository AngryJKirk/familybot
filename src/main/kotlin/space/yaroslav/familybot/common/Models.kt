package space.yaroslav.familybot.common

import java.time.Instant
import space.yaroslav.familybot.route.models.Command

data class User(val id: Long, val chat: Chat, val name: String?, val nickname: String?) {

    fun getGeneralName(mention: Boolean = true): String {
        return if (mention) {
            if (nickname != null) {
                "@$nickname"
            } else {
                "<a href=\"tg://user?id=$id\">$name</a>"
            }
        } else {
            name ?: "хуй знает кто"
        }
    }
}

data class Chat(val id: Long, val name: String?)

data class Pidor(val user: User, val date: Instant)

data class CommandByUser(val user: User, val command: Command, val date: Instant)

enum class Pluralization {
    ONE,
    FEW,
    MANY;

    companion object PluralizationCalc {
        fun getPlur(position: Int): Pluralization {
            return if (position % 10 == 1 && position % 100 != 11) ONE
            else if (position % 10 in 2..4 && (position % 100 < 10 || position % 100 >= 20)) FEW
            else MANY
        }
    }
}

data class AskWorldQuestion(
    val id: Long?,
    val message: String,
    val user: User,
    val chat: Chat,
    val date: Instant,
    val messageId: Long?
)

data class AskWorldReply(
    val id: Long?,
    val questionId: Long,
    val message: String,
    val user: User,
    val chat: Chat,
    val date: Instant
)

data class CustomMessage(val id: Long, val chat: Chat, val message: String)
