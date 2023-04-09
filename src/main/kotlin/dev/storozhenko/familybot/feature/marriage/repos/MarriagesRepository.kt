package dev.storozhenko.familybot.feature.marriage.repos

import dev.storozhenko.familybot.common.extensions.toMarriage
import dev.storozhenko.familybot.feature.marriage.model.Marriage
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.time.Instant

@Component
class MarriagesRepository(private val jdbcTemplate: JdbcTemplate) {

    fun getMarriage(chatId: Long, userId: Long): Marriage? {
        return getAllMarriages(chatId)
            .find { (_, firstUser, secondUser) ->
                firstUser.id == userId || secondUser.id == userId
            }
    }

    fun addMarriage(marriage: Marriage) {
        jdbcTemplate.update(
            """
           INSERT INTO marriages (marriage_id,
                                  marriage_start_date,
                                  chat_id,
                                  first_user,
                                  second_user)
           VALUES (gen_random_uuid(), ?, ?, ?, ?)
            """.trimIndent(),
            Timestamp.from(marriage.startDate),
            marriage.chatId,
            marriage.firstUser.id,
            marriage.secondUser.id
        )
    }

    fun getAllMarriages(chatId: Long): List<Marriage> {
        return jdbcTemplate.query(
            """
            SELECT m.chat_id,
            m.marriage_start_date,
            c.id AS chat_id,
            c.name AS chat_name,
            u1.id AS user_id_1,
            u1.name AS user_name_1,
            u1.username AS user_username_1,
            u2.id AS user_id_2,
            u2.name AS user_name_2,
            u2.username AS user_username_2
            FROM marriages m
            INNER JOIN chats c ON c.id = m.chat_id
            INNER JOIN users u1 ON m.first_user = u1.id
            INNER JOIN users u2 ON m.second_user = u2.id
            WHERE chat_id = ? AND marriage_end_date IS NULL
            """.trimIndent(),
            { rs, _ -> rs.toMarriage() },
            chatId
        )
    }

    fun removeMarriage(chatId: Long, userId: Long) {
        jdbcTemplate.update(
            """
            UPDATE marriages
            SET marriage_end_date = ?
            WHERE chat_id = ?
            AND (first_user = ? OR second_user =?)
            """.trimIndent(),
            Timestamp.from(Instant.now()),
            chatId,
            userId,
            userId
        )
    }
}
