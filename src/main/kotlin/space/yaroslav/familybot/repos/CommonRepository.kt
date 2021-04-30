package space.yaroslav.familybot.repos

import io.micrometer.core.annotation.Timed
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
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import javax.sql.DataSource

@Component
class CommonRepository(datasource: DataSource) {

    private val template = JdbcTemplate(datasource)

    @Timed("repository.CommonRepository.addUser")
    fun addUser(user: User) {
        template.update(
            "INSERT INTO users (id, name, username) VALUES (?, ?, ?) ON CONFLICT(id) DO UPDATE SET name = excluded.name, username = excluded.username ",
            user.id,
            user.name,
            user.nickname
        )
        template.update(
            "INSERT INTO users2chats (chat_id, user_id) VALUES (?, ?) ON CONFLICT(chat_id, user_id) DO UPDATE SET active = TRUE ",
            user.chat.id,
            user.id
        )
    }

    @Timed("repository.CommonRepository.getUsers")
    fun getUsers(chat: Chat, activeOnly: Boolean = false): List<User> {
        var select = "SELECT * FROM users INNER JOIN users2chats u ON users.id = u.user_id WHERE u.chat_id = ${chat.id}"
        if (activeOnly) {
            select += "and u.active = true"
        }
        return template.query(select) { rs, _ -> rs.toUser() }
    }

    @Timed("repository.CommonRepository.addChat")
    fun addChat(chat: Chat) {
        template.update(
            "INSERT INTO chats (id, name) VALUES (?, ?) ON CONFLICT(id) DO UPDATE SET name = excluded.name, active = TRUE",
            chat.id,
            chat.name
                ?: ""
        )
    }

    @Timed("repository.CommonRepository.getChats")
    fun getChats(): List<Chat> {
        return template.query("SELECT * FROM chats WHERE active = TRUE ") { rs, _ -> rs.toChat() }
    }

    @Timed("repository.CommonRepository.addPidor")
    fun addPidor(pidor: Pidor) {
        template.update(
            "INSERT INTO pidors (id, pidor_date, chat_id) VALUES (?, ?, ?)",
            pidor.user.id,
            Timestamp.from(pidor.date),
            pidor.user.chat.id
        )
    }

    @Timed("repository.CommonRepository.removePidorRecord")
    fun removePidorRecord(user: User): Int {
        return template.update(
            "DELETE FROM pidors WHERE id = ? AND chat_id = ? AND pidor_date = (SELECT pidor_date FROM pidors WHERE id = ? AND chat_id = ? AND pidor_date > DATE_TRUNC('month', CURRENT_DATE) LIMIT 1)",
            user.id,
            user.chat.id,
            user.id,
            user.chat.id
        )
    }

    @Timed("repository.CommonRepository.removePidorRecords")
    fun removePidorRecords(chat: Chat, from: Instant, until: Instant): Int {
        return template.update(
            "DELETE FROM pidors WHERE chat_id = ? AND pidor_date >= ? AND pidor_date <= ?",
            chat.id,
            Timestamp.from(from),
            Timestamp.from(until)
        )
    }

    @Timed("repository.CommonRepository.getPidorsByChat")
    fun getPidorsByChat(
        chat: Chat,
        startDate: Instant = Instant.ofEpochMilli(969652800000),
        endDate: Instant = Instant.now()
    ): List<Pidor> {
        return template.query(
            "SELECT * FROM pidors INNER JOIN users u ON pidors.id = u.id WHERE pidors.chat_id = ? AND pidor_date BETWEEN ? AND ?",
            ResultSetExtractor { resultSet -> resultSet.map(ResultSet::toPidor) },
            chat.id,
            Timestamp.from(startDate),
            Timestamp.from(endDate)
        ) ?: emptyList()
    }

    @Timed("repository.CommonRepository.getAllPidors")
    fun getAllPidors(
        startDate: Instant = Instant.ofEpochMilli(969652800000),
        endDate: Instant = Instant.now()
    ): List<Pidor> {
        return template.query(
            "SELECT * FROM pidors INNER JOIN users u ON pidors.id = u.id WHERE pidor_date BETWEEN ? AND ?",
            ResultSetExtractor { resultSet -> resultSet.map(ResultSet::toPidor) },
            Timestamp.from(startDate),
            Timestamp.from(endDate)
        ) ?: emptyList()
    }

    @Timed("repository.CommonRepository.changeChatActiveStatus")
    fun changeChatActiveStatus(chat: Chat, status: Boolean) {
        template.update("UPDATE chats SET active = ? WHERE id = ?", status, chat.id)
    }

    @Timed("repository.CommonRepository.changeUserActiveStatusNew")
    fun changeUserActiveStatusNew(user: User, status: Boolean) {
        template.update(
            "UPDATE users2chats SET active = ? WHERE chat_id = ? AND user_id = ?",
            status,
            user.chat.id,
            user.id
        )
    }

    @Timed("repository.CommonRepository.disableUsersInChat")
    fun disableUsersInChat(chat: Chat) {
        template.update("UPDATE users2chats SET active = FALSE WHERE chat_id = ?", chat.id)
    }

    @Timed("repository.CommonRepository.findUsersByName")
    fun findUsersByName(namePart: String): List<User> {
        return template.query(
            "SELECT * FROM users INNER JOIN users2chats u2c ON users.id = u2c.user_id WHERE LOWER(name) LIKE LOWER(?) OR LOWER(username) LIKE LOWER(?)",
            { rs, _ -> rs.toUser() }, "%$namePart%", "%$namePart%"
        )
    }

    @Timed("repository.CommonRepository.getChatsByUser")
    fun getChatsByUser(user: User): List<Chat> {
        return template.query(
            "SELECT * FROM chats INNER JOIN users2chats u2c ON chats.id = u2c.chat_id WHERE u2c.user_id = ? ",
            { rs, _ -> rs.toChat() }, user.id
        )
    }
}
