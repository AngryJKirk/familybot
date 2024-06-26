package dev.storozhenko.familybot.feature.logging.repos

import dev.storozhenko.familybot.common.extensions.map
import dev.storozhenko.familybot.common.extensions.toCommandByUser
import dev.storozhenko.familybot.core.models.telegram.CommandByUser
import dev.storozhenko.familybot.core.models.telegram.User
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class CommandHistoryRepository(val template: JdbcTemplate) {

    fun add(commandByUser: CommandByUser) {
        template.update(
            "INSERT INTO history (command_id, user_id, chat_id, command_date) VALUES (?, ?, ?, ?)",
            commandByUser.command.id,
            commandByUser.user.id,
            commandByUser.user.chat.id,
            Timestamp.from(commandByUser.date),
        )
    }

    fun get(
        user: User,
        from: Instant = Instant.now().minus(5, ChronoUnit.MINUTES),
        to: Instant = Instant.now(),
    ): List<CommandByUser> {
        return template.query(
            "SELECT * FROM history WHERE user_id = ? AND chat_id = ? AND command_date BETWEEN ? AND ?",
            ResultSetExtractor { resultSet -> resultSet.map { it.toCommandByUser(user) } },
            user.id,
            user.chat.id,
            Timestamp.from(from),
            Timestamp.from(to),
        ) ?: emptyList()
    }
}
