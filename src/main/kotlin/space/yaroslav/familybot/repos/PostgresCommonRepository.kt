package space.yaroslav.familybot.repos

import io.micrometer.core.annotation.Timed
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.Pidor
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.map
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toPidor
import space.yaroslav.familybot.common.utils.toUser
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
    @Timed("repository.CommonRepository.addUser")
    override fun addUser(user: User) {
        template.update(
            "INSERT INTO users (id, name, username) VALUES (?, ?, ?) ON CONFLICT(id) DO UPDATE SET name = EXCLUDED.name, username = EXCLUDED.username ",
            user.id,
            user.name,
            user.nickname
        )
        template.update(
            "INSERT INTO users2chats (chat_id, user_id) VALUES (?, ?) ON CONFLICT(chat_id, user_id) DO UPDATE SET active = true ",
            user.chat.id,
            user.id
        )
    }

    @Timed("repository.CommonRepository.getUsers")
    override fun getUsers(chat: Chat, activeOnly: Boolean): List<User> {
        var select = "SELECT * FROM users INNER JOIN users2chats u ON users.id = u.user_id WHERE u.chat_id = ${chat.id}"
        if (activeOnly) {
            select += "and u.active = true"
        }
        return template.query(select) { rs, _ -> rs.toUser() }
    }

    @Timed("repository.CommonRepository.addChat")
    override fun addChat(chat: Chat) {
        template.update(
            "INSERT INTO chats (id, name) VALUES (?, ?) ON CONFLICT(id) DO UPDATE SET name = EXCLUDED.name, active = true",
            chat.id,
            chat.name
                ?: ""
        )
    }

    @Timed("repository.CommonRepository.getChats")
    override fun getChats(): List<Chat> {
        return template.query("SELECT * FROM chats where active = true ") { rs, _ -> rs.toChat() }
    }

    @Timed("repository.CommonRepository.addPidor")
    override fun addPidor(pidor: Pidor) {
        template.update(
            "INSERT INTO pidors (id, pidor_date, chat_id) VALUES (?, ?, ?)",
            pidor.user.id,
            Timestamp.from(pidor.date),
            pidor.user.chat.id
        )
    }

    @Timed("repository.CommonRepository.removePidorRecord")
    override fun removePidorRecord(user: User) {
        template.update(
            "DELETE FROM pidors where id = ? and chat_id = ? and pidor_date = (SELECT pidor_date from pidors where id = ? and chat_id = ? and pidor_date > date_trunc('month', current_date) LIMIT 1)",
            user.id,
            user.chat.id,
            user.id,
            user.chat.id
        )
    }

    @Timed("repository.CommonRepository.getPidorsByChat")
    override fun getPidorsByChat(chat: Chat, startDate: Instant, endDate: Instant): List<Pidor> {
        return template.query(
            "SELECT * FROM pidors INNER JOIN users u ON pidors.id = u.id WHERE pidors.chat_id = ? AND pidor_date BETWEEN ? and ?",
            ResultSetExtractor { resultSet -> resultSet.map { it.toPidor() } },
            chat.id,
            Timestamp.from(startDate),
            Timestamp.from(endDate)
        ) ?: emptyList()
    }

    @Timed("repository.CommonRepository.containsUser")
    override fun containsUser(user: User): Boolean {
        if (userCache.contains(user)) {
            return true
        }
        val exist =
            template.query("SELECT * FROM users WHERE id = ?", { rs, _ -> rs.toUser() }, user.id).isNotEmpty()
        if (exist) {
            userCache.add(user)
        }
        return exist
    }

    @Timed("repository.CommonRepository.containsChat")
    override fun containsChat(chat: Chat): Boolean {
        if (chatCache.contains(chat)) {
            return true
        }
        val exist =
            template.query("SELECT * FROM chats WHERE id = ?", { rs, _ -> rs.toChat() }, chat.id).isNotEmpty()
        if (exist) {
            chatCache.add(chat)
        }
        return exist
    }

    @Timed("repository.CommonRepository.changeUserActiveStatus")
    override fun changeUserActiveStatus(user: User, status: Boolean) {
        template.update("UPDATE users SET active = ? WHERE id = ?", status, user.id)
    }

    @Timed("repository.CommonRepository.getAllPidors")
    override fun getAllPidors(startDate: Instant, endDate: Instant): List<Pidor> {
        return template.query(
            "SELECT * FROM pidors INNER JOIN users u ON pidors.id = u.id WHERE pidor_date BETWEEN ? and ?",
            ResultSetExtractor { resultSet -> resultSet.map { it.toPidor() } },
            Timestamp.from(startDate),
            Timestamp.from(endDate)
        ) ?: emptyList()
    }

    @Timed("repository.CommonRepository.changeChatActiveStatus")
    override fun changeChatActiveStatus(chat: Chat, status: Boolean) {
        template.update("update chats set active = ? where id = ?", status, chat.id)
    }

    @Timed("repository.CommonRepository.changeUserActiveStatusNew")
    override fun changeUserActiveStatusNew(user: User, status: Boolean) {
        template.update(
            "update users2chats set active = ? where chat_id = ? and user_id = ?",
            status,
            user.chat.id,
            user.id
        )
    }

    @Timed("repository.CommonRepository.disableUsersInChat")
    override fun disableUsersInChat(chat: Chat) {
        template.update("update users2chats set active = false where chat_id = ?", chat.id)
    }
}
