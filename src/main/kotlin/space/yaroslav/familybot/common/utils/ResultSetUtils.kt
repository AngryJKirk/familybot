package space.yaroslav.familybot.common.utils

import space.yaroslav.familybot.common.AskWorldQuestion
import space.yaroslav.familybot.common.AskWorldReply
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.CommandByUser
import space.yaroslav.familybot.common.Pidor
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.route.models.Command
import java.sql.ResultSet


fun ResultSet.toUser(): User = User(
        this.getLong("id"),
        Chat(this.getLong("chat_id"), ""),
        this.getString("name"),
        this.getString("username"))

fun ResultSet.toChat(): Chat = Chat(
        this.getLong("id"),
        this.getString("name")
)

fun ResultSet.toPidor(): Pidor = Pidor(
        this.toUser(),
        this.getTimestamp("pidor_date").toInstant())

fun ResultSet.toCommandByUser(user: User?): CommandByUser {
    val userInternal = user ?: this.toUser()
    return CommandByUser(userInternal, Command.values().find { it.id == this.getInt("command_id") }!!,
            this.getTimestamp("command_date").toInstant())
}

fun ResultSet.toAskWorldQuestion(): AskWorldQuestion {
    val chat = Chat(this.getLong("chat_id"), this.getString("chat_name"))
    return AskWorldQuestion(this.getLong("id"),
            this.getString("question"),
            User(this.getLong("user_id"),
                    chat,
                    this.getString("common_name"),
                    this.getString("username")),
            chat,
            this.getTimestamp("date").toInstant(), null)
}


fun ResultSet.toAskWorldReply(): AskWorldReply {
    val chat = Chat(this.getLong("chat_id"), this.getString("chat_name"))
    return AskWorldReply(
            this.getLong("id"),
            this.getLong("question_id"),
            this.getString("reply"),
            User(this.getLong("user_id"),
                    chat,
                    this.getString("common_name"),
                    this.getString("username")),
            chat,
            this.getTimestamp("date").toInstant())
}


fun <T> ResultSet.map(action: (ResultSet) -> T): List<T> {
    val result = ArrayList<T>()
    while (next()) {
        result.add(action.invoke(this))
    }
    return result
}
