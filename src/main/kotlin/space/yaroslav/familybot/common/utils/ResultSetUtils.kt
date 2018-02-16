package space.yaroslav.familybot.common.utils

import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.Pidor
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.repos.ifaces.CommandByUser
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

fun <T> ResultSet.map(action: (ResultSet) -> T): List<T> {
    val result = ArrayList<T>()
    while (next()) {
        result.add(action.invoke(this))
    }
    return result
}