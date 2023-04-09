package dev.storozhenko.familybot.feature.logging.repos

import dev.storozhenko.familybot.common.extensions.DateConstants
import dev.storozhenko.familybot.common.extensions.map
import dev.storozhenko.familybot.common.extensions.toCommandByUser
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.models.telegram.CommandByUser
import dev.storozhenko.familybot.core.models.telegram.User
import io.micrometer.core.annotation.Timed
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class CommandHistoryRepository(val template: JdbcTemplate) {

    @Timed("repository.CommandHistoryRepository.getAll")
    fun getAll(chat: Chat, from: Instant = DateConstants.theBirthDayOfFamilyBot): List<CommandByUser> {
        return template.query(
            "SELECT * FROM history INNER JOIN users u ON history.user_id = u.id AND history.chat_id = ? AND history.command_date >= ?",
            { rs, _ -> rs.toCommandByUser(null) },
            chat.id,
            Timestamp.from(from)
        )
    }

    @Timed("repository.CommandHistoryRepository.add")
    fun add(commandByUser: CommandByUser) {
        template.update(
            "INSERT INTO history (command_id, user_id, chat_id, command_date) VALUES (?, ?, ?, ?)",
            commandByUser.command.id,
            commandByUser.user.id,
            commandByUser.user.chat.id,
            Timestamp.from(commandByUser.date)
        )
    }

    @Timed("repository.CommandHistoryRepository.get")
    fun get(
        user: User,
        from: Instant = Instant.now().minus(5, ChronoUnit.MINUTES),
        to: Instant = Instant.now()
    ): List<CommandByUser> {
        return template.query(
            "SELECT * FROM history WHERE user_id = ? AND chat_id = ? AND command_date BETWEEN ? AND ?",
            ResultSetExtractor { resultSet -> resultSet.map { it.toCommandByUser(user) } },
            user.id,
            user.chat.id,
            Timestamp.from(from),
            Timestamp.from(to)
        ) ?: emptyList()
    }
}
