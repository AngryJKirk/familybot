package space.yaroslav.familybot.repos

import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.*
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.*
import javax.sql.DataSource

@Component
@Primary
class PostgresCommonRepository(datasource: DataSource) : CommonRepository {

    val template = JdbcTemplate(datasource)

    override fun addUser(user: User) {
        template.update("INSERT INTO users (id, chat_id, name, username) VALUES (?, ?, ?, ?) ON CONFLICT(id) DO UPDATE SET name = EXCLUDED.name, username = EXCLUDED.username",
                user.id, user.chat.id, user.name.removeEmoji(), user.nickname)
    }

    override fun getUsers(chat: Chat): List<User> {
        return template.query("SELECT * FROM users WHERE chat_id = ${chat.id}", { rs, _ -> rs.toUser() })
    }

    override fun addChat(chat: Chat) {
        template.update("INSERT INTO chats (id, name) VALUES (${chat.id}, '${chat.name ?: ""}')")
    }

    override fun getChats(): List<Chat> {
        return template.query("SELECT * FROM chats", { rs, _ -> rs.toChat() })
    }

    override fun addPidor(pidor: Pidor) {
        template.update("INSERT INTO pidors (id, pidor_date) VALUES (${pidor.user.id}, ?)",
                Timestamp.from(pidor.date))
    }

    override fun getPidorsByChat(chat: Chat, startDate: Instant, endDate: Instant): List<Pidor> {
        return template.query("SELECT * FROM pidors INNER JOIN users u ON pidors.id = u.id WHERE chat_id = ${chat.id} AND pidor_date BETWEEN ? and ?",
                ResultSetExtractor { it.map { it.toPidor() } }, Timestamp.from(startDate), Timestamp.from(endDate))
    }

    override fun containsUser(user: User): Boolean {
        return template.query("SELECT * FROM users WHERE id = ${user.id}", { rs, _ -> rs.toUser() }).isNotEmpty()
    }

    override fun containsChat(chat: Chat): Boolean {
        return template.query("SELECT * FROM chats WHERE id = ${chat.id}", { rs, _ -> rs.toChat() }).isNotEmpty()
    }


}


