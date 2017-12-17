package space.yaroslav.familybot.repos

import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.Pidor
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.removeEmoji
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.time.ZoneOffset
import javax.sql.DataSource

@Component
@Primary
class PostgresCommonRepository(datasource: DataSource) : CommonRepository {

    val template = JdbcTemplate(datasource)

    override fun addUser(user: User) {
        template.update("INSERT INTO users (id, chat_id, name, username) " +
                "VALUES (${user.id}, ${user.chat.id}, '${user.name.removeEmoji()}', '${user.nickname}')")
    }

    override fun getUsers(chat: Chat): List<User> {
        return template.query("SELECT * FROM users WHERE chat_id = ${chat.id}", { rs, _ -> toUser(rs) })
    }

    override fun addChat(chat: Chat) {
        template.update("INSERT INTO chats (id, name) VALUES (${chat.id}, '${chat.name?:""}')")
    }

    override fun getChats(): List<Chat> {
        return template.query("SELECT * FROM chats", { rs, _ -> toChat(rs) })
    }

    override fun addPidor(pidor: Pidor) {
        template.update("INSERT INTO pidors (id, pidor_date) VALUES (${pidor.user.id}, ?)",
                Timestamp.from(pidor.date.toInstant(ZoneOffset.UTC)))
    }

    override fun getPidorsByChat(chat: Chat, startDate: Instant, endDate: Instant): List<Pidor> {
        return template.query("SELECT * FROM pidors INNER JOIN users u ON pidors.id = u.id WHERE chat_id = ${chat.id}", { rs, _ -> toPidor(rs) })
    }

    override fun containsUser(user: User): Boolean {
        return template.query("SELECT * FROM users WHERE id = ${user.id}", { rs, _ -> toUser(rs) }).isNotEmpty()
    }

    override fun containsChat(chat: Chat): Boolean {
        return template.query("SELECT * FROM chats WHERE id = ${chat.id}", { rs, _ -> toChat(rs) }).isNotEmpty()
    }

    fun toUser(result: ResultSet): User = User(
            result.getLong("id"),
            Chat(result.getLong("chat_id"), ""),
            result.getString("name"),
            result.getString("username"))

    fun toChat(result: ResultSet): Chat = Chat(
            result.getLong("id"),
            result.getString("name")
    )

    fun toPidor(result: ResultSet): Pidor = Pidor(
            toUser(result),
            result.getTimestamp("pidor_date").toLocalDateTime())

}