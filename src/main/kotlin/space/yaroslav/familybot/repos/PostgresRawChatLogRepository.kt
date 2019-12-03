package space.yaroslav.familybot.repos

import java.sql.Timestamp
import java.time.Instant
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.repos.ifaces.RawChatLogRepository

@Component
class PostgresRawChatLogRepository(val template: JdbcTemplate) : RawChatLogRepository {

    override fun getMessageCount(chat: Chat, user: User): Int {
        return template.queryForObject(
            "SELECT count(*) from raw_chat_log where chat_id = ? and user_id = ?",
            Int::class.java, chat.id, user.id
        )
    }

    override fun add(chat: Chat, user: User, message: String?, fileId: String?, rawUpdate: String, date: Instant) {
        template.update(
            "INSERT INTO raw_chat_log (chat_id, user_id, message, raw_update, date, file_id) VALUES (?, ?, ?, ?::JSON, ?, ?)",
            chat.id, user.id, message, rawUpdate, Timestamp.from(date), fileId
        )
    }
}
