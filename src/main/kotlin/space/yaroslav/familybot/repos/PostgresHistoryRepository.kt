package space.yaroslav.familybot.repos

import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.map
import space.yaroslav.familybot.repos.ifaces.CommandByUser
import space.yaroslav.familybot.repos.ifaces.HistoryRepository
import space.yaroslav.familybot.route.models.Command
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant

@Component
@Primary
class PostgresHistoryRepository(val template: JdbcTemplate) : HistoryRepository {
    override fun add(commandByUser: CommandByUser) {
        template.update("INSERT INTO history (command_id, user_id, command_date) VALUES (?, ?, ?)",
                commandByUser.command.id, commandByUser.user.id, Timestamp.from(commandByUser.date))
    }

    override fun get(user: User, from: Instant, to: Instant): List<CommandByUser> {
       return template.query("SELECT * FROM history WHERE user_id = ${user.id} and command_date BETWEEN ? and ?",
                ResultSetExtractor { it.map { toCommandByUser(it, user) } }, Timestamp.from(from), Timestamp.from(to))
    }

    fun toCommandByUser(rs: ResultSet, user: User): CommandByUser {
        return CommandByUser(user, Command.values().find { it.id == rs.getInt("command_id") }!!,
                rs.getTimestamp("command_date").toInstant())
    }
}