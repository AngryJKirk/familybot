package space.yaroslav.familybot.repos

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.CommandByUser
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.map
import space.yaroslav.familybot.common.utils.toCommandByUser
import space.yaroslav.familybot.repos.ifaces.CommandHistoryRepository
import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class PostgresCommandHistoryRepository(val template: JdbcTemplate) : CommandHistoryRepository {

    private val defaultAmountOfDaysToSubtract = 100000L // fix that in 2294 a.c. please

    override fun getAll(chat: Chat, from: Instant?): List<CommandByUser> {
        val fromDate = from ?: Instant.now().minus(defaultAmountOfDaysToSubtract, ChronoUnit.DAYS)
        return template.query(
            "SELECT * FROM history INNER JOIN users u ON history.user_id = u.id AND history.chat_id = ? and history.command_date >= ?",
            { rs, _ -> rs.toCommandByUser(null) },
            chat.id,
            Timestamp.from(fromDate)
        )
    }

    override fun getTheFirst(chat: Chat): CommandByUser? {
        return template.query(
            "SELECT * FROM history INNER JOIN users u ON history.user_id = u.id AND history.chat_id = ? " +
                "order by command_date limit 1",
            { rs, _ -> rs.toCommandByUser(null) },
            chat.id
        ).firstOrNull()
    }

    override fun add(commandByUser: CommandByUser) {
        template.update(
            "INSERT INTO history (command_id, user_id, chat_id, command_date) VALUES (?, ?, ?, ?)",
            commandByUser.command.id,
            commandByUser.user.id,
            commandByUser.user.chat.id,
            Timestamp.from(commandByUser.date)
        )
    }

    override fun get(user: User, from: Instant, to: Instant): List<CommandByUser> {
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
