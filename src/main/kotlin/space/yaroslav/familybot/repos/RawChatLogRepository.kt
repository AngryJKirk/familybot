package space.yaroslav.familybot.repos

import io.micrometer.core.annotation.Timed
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import java.sql.Timestamp
import java.time.Instant

@Component
class RawChatLogRepository(val template: JdbcTemplate) {

    @Timed("repository.RawChatLogRepository.getMessageCount")
    fun getMessageCount(chat: Chat, user: User): Int {
        return template.queryForObject(
            "SELECT COUNT(*) FROM raw_chat_log WHERE chat_id = ? AND user_id = ?",
            Int::class.java,
            chat.id,
            user.id
        )
    }

    @Timed("repository.RawChatLogRepository.add")
    fun add(chat: Chat, user: User, message: String?, fileId: String?, rawUpdate: String, date: Instant) {
        template.update(
            "INSERT INTO raw_chat_log (chat_id, user_id, message, raw_update, date, file_id) VALUES (?, ?, ?, ?::JSON, ?, ?)",
            chat.id,
            user.id,
            message,
            rawUpdate,
            Timestamp.from(date),
            fileId
        )
    }
}
