package space.yaroslav.familybot.repos

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.CommandByUser
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.map
import space.yaroslav.familybot.common.utils.toCommandByUser
import space.yaroslav.familybot.repos.ifaces.HistoryRepository
import java.sql.Timestamp
import java.time.Instant

@Component
class PostgresHistoryRepository(val template: JdbcTemplate) : HistoryRepository {
    override fun getAll(chat: Chat): List<CommandByUser> {
        return template.query("SELECT * FROM history INNER JOIN users u ON history.user_id = u.id AND history.chat_id = ?",
                RowMapper { rs, _ -> rs.toCommandByUser(null) }, chat.id)
    }

    override fun add(commandByUser: CommandByUser) {
        template.update("INSERT INTO history (command_id, user_id, chat_id, command_date) VALUES (?, ?, ?, ?)",
                commandByUser.command.id, commandByUser.user.id, commandByUser.user.chat.id, Timestamp.from(commandByUser.date))
    }

    override fun get(user: User, from: Instant, to: Instant): List<CommandByUser> {
        return template.query("SELECT * FROM history WHERE user_id = ? AND chat_id = ? AND command_date BETWEEN ? AND ?",
                ResultSetExtractor { it.map { it.toCommandByUser(user) } }, user.id, user.chat.id, Timestamp.from(from), Timestamp.from(to))
    }


}