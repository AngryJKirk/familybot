package dev.storozhenko.familybot.feature.logging.repos

import dev.storozhenko.familybot.common.extensions.toUser
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.models.telegram.User
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class RawChatLogRepository(private val template: JdbcTemplate) {

    fun getMessageCount(chat: Chat, user: User): Int {
        return template.queryForObject(
            "SELECT COUNT(*) FROM raw_chat_log WHERE chat_id = ? AND user_id = ?",
            Int::class.java,
            chat.id,
            user.id,
        )
    }

    fun add(chat: Chat, user: User, message: String?, messageId: Long, fileId: String?, rawUpdate: String, date: Instant) {
        template.update(
            "INSERT INTO raw_chat_log (chat_id, user_id, msg_id, message, raw_update, date, file_id) VALUES (?, ?, ?, ?, ?::JSON, ?, ?)",
            chat.id,
            user.id,
            messageId,
            message,
            rawUpdate,
            Timestamp.from(date),
            fileId,
        )
    }

    fun getMessages(
        chat: Chat,
        from: Instant = Instant.now().minus(30, ChronoUnit.DAYS),
        limit: Int = 100
    ): List<Pair<User, String>> {
        return template.query(
            """
            select u.id as id, u.name as name, u.username as username, message from raw_chat_log 
            join public.users u on u.id = raw_chat_log.user_id
            where chat_id = ? and date >= ? and message is not null and message not like '/%' order by date desc limit $limit
        """.trimIndent(),
            { rs, _ -> rs.toUser(chat) to rs.getString("message") },
            chat.id,
            Timestamp.from(from)
        )
    }
}
