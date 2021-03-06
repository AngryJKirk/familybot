package space.yaroslav.familybot.repos

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.models.FunctionId
import space.yaroslav.familybot.repos.ifaces.FunctionsConfigureRepository

@Component
class PostgresFunctionsConfigureRepository(val jdbcTemplate: JdbcTemplate) : FunctionsConfigureRepository {
    override fun isEnabled(id: FunctionId, chat: Chat): Boolean {
        return jdbcTemplate.query(
            "SELECT active FROM function_settings WHERE function_id = ? AND chat_id = ? ORDER BY date_from DESC LIMIT 1",
            { rs, _ -> rs.getBoolean("active") },
            id.id,
            chat.id
        ).firstOrNull() ?: true
    }

    override fun switch(id: FunctionId, chat: Chat) {
        jdbcTemplate.update(
            "INSERT INTO function_settings (function_id, chat_id, active) VALUES (?, ?, ?)",
            id.id,
            chat.id,
            !isEnabled(id, chat)
        )
    }
}
