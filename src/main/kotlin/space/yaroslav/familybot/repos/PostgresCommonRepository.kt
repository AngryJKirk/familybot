package space.yaroslav.familybot.repos

import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.Pidor
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.*
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import java.sql.Timestamp
import java.time.Instant
import javax.sql.DataSource

@Component
@Primary
class PostgresCommonRepository(datasource: DataSource) : CommonRepository {
    private val template = JdbcTemplate(datasource)

    private val chatCache: MutableSet<Chat> = HashSet()
    private val userCache: MutableSet<User> = HashSet()
    override fun addUser(user: User) {
        template.update("INSERT INTO users (id, name, username) VALUES (?, ?, ?) ON CONFLICT(id) DO UPDATE SET name = EXCLUDED.name, username = EXCLUDED.username, active = TRUE ",
                user.id, user.name.removeEmoji(), user.nickname)
        template.update("INSERT INTO users2chats (chat_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
                user.chat.id, user.id)
    }


    override fun getUsers(chat: Chat, activeOnly: Boolean): List<User> {
        var select = "SELECT * FROM users INNER JOIN users2chats u ON users.id = u.user_id WHERE u.chat_id = ${chat.id}"
        if(activeOnly){
            select += "and active = true"
        }
        return template.query(select, { rs, _ -> rs.toUser() })
    }

    override fun addChat(chat: Chat) {
        template.update("INSERT INTO chats (id, name) VALUES (?, ?)", chat.id, chat.name ?: "")
    }

    override fun getChats(): List<Chat> {
        return template.query("SELECT * FROM chats", { rs, _ -> rs.toChat() })
    }

    override fun addPidor(pidor: Pidor) {
        template.update("INSERT INTO pidors (id, pidor_date, chat_id) VALUES (${pidor.user.id}, ?, ${pidor.user.chat.id})",
                Timestamp.from(pidor.date))
    }

    override fun removePidorRecord(user: User) {
        template.update("DELETE FROM pidors where id = ? and pidor_date = (SELECT pidor_date from pidors where id = ? and pidor_date > date_trunc('month', current_date) LIMIT 1)",
                user.id, user.id)
    }

    override fun getPidorsByChat(chat: Chat, startDate: Instant, endDate: Instant): List<Pidor> {
        return template.query("SELECT * FROM pidors INNER JOIN users u ON pidors.id = u.id WHERE pidors.chat_id = ${chat.id} AND pidor_date BETWEEN ? and ?",
                ResultSetExtractor { it.map { it.toPidor() } }, Timestamp.from(startDate), Timestamp.from(endDate))
    }

    override fun containsUser(user: User): Boolean {
        if (userCache.contains(user)) {
            return true
        }
        val exist = template.query("SELECT * FROM users WHERE id = ${user.id}", { rs, _ -> rs.toUser() }).isNotEmpty()
        if (exist) {
            userCache.add(user)
        }
        return exist
    }

    override fun containsChat(chat: Chat): Boolean {
        if (chatCache.contains(chat)) {
            return true
        }
        val exist = template.query("SELECT * FROM chats WHERE id = ${chat.id}", { rs, _ -> rs.toChat() }).isNotEmpty()
        if (exist) {
            chatCache.add(chat)
        }
        return exist
    }

    override fun changeUserActiveStatus(user: User, status: Boolean) {
        template.update("UPDATE users SET active = $status WHERE id = ${user.id}")
    }



}


