package dev.storozhenko.familybot.feature.pidor.repos

import dev.storozhenko.familybot.common.extensions.DateConstants
import dev.storozhenko.familybot.common.extensions.map
import dev.storozhenko.familybot.common.extensions.toPidor
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.models.telegram.User
import dev.storozhenko.familybot.feature.pidor.models.Pidor
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant

@Component
class PidorRepository(private val template: JdbcTemplate) {

    fun addPidor(pidor: Pidor) {
        template.update(
            "INSERT INTO pidors (id, pidor_date, chat_id) VALUES (?, ?, ?)",
            pidor.user.id,
            Timestamp.from(pidor.date),
            pidor.user.chat.id,
        )
    }

    fun removePidorRecord(user: User): Int {
        return template.update(
            "DELETE FROM pidors WHERE id = ? AND chat_id = ? AND pidor_date = (SELECT pidor_date FROM pidors WHERE id = ? AND chat_id = ? AND pidor_date > DATE_TRUNC('month', CURRENT_DATE) LIMIT 1)",
            user.id,
            user.chat.id,
            user.id,
            user.chat.id,
        )
    }

    fun removePidorRecords(chat: Chat, from: Instant, until: Instant): Int {
        return template.update(
            "DELETE FROM pidors WHERE chat_id = ? AND pidor_date >= ? AND pidor_date <= ?",
            chat.id,
            Timestamp.from(from),
            Timestamp.from(until),
        )
    }

    fun getPidorsByChat(
        chat: Chat,
        startDate: Instant = DateConstants.theBirthDayOfFamilyBot,
        endDate: Instant = Instant.now(),
    ): List<Pidor> {
        return template.query(
            "SELECT * FROM pidors INNER JOIN users u ON pidors.id = u.id WHERE pidors.chat_id = ? AND pidor_date BETWEEN ? AND ?",
            ResultSetExtractor { resultSet -> resultSet.map(ResultSet::toPidor) },
            chat.id,
            Timestamp.from(startDate),
            Timestamp.from(endDate),
        ) ?: emptyList()
    }

    fun getAllPidors(
        startDate: Instant = DateConstants.theBirthDayOfFamilyBot,
        endDate: Instant = Instant.now(),
    ): List<Pidor> {
        return template.query(
            "SELECT * FROM pidors INNER JOIN users u ON pidors.id = u.id WHERE pidor_date BETWEEN ? AND ?",
            ResultSetExtractor { resultSet -> resultSet.map(ResultSet::toPidor) },
            Timestamp.from(startDate),
            Timestamp.from(endDate),
        ) ?: emptyList()
    }
}
