package space.yaroslav.familybot.repos

import io.micrometer.core.annotation.Timed
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.models.FunctionId

@Component
class PostgresFunctionsConfigureRepository(val jdbcTemplate: JdbcTemplate) : FunctionsConfigureRepository {
    @Timed("repository.PostgresFunctionsConfigureRepository.isEnabled")
    override fun isEnabled(id: FunctionId, chat: Chat): Boolean {
        return jdbcTemplate.query(
            "SELECT active FROM function_settings WHERE function_id = ? AND chat_id = ? ORDER BY date_from DESC LIMIT 1",
            { rs, _ -> rs.getBoolean("active") },
            id.id,
            chat.id
        ).firstOrNull() ?: true
    }

    @Timed("repository.PostgresFunctionsConfigureRepository.switch")
    override suspend fun switch(id: FunctionId, chat: Chat) {
        jdbcTemplate.update(
            "INSERT INTO function_settings (function_id, chat_id, active) VALUES (?, ?, ?)",
            id.id,
            chat.id,
            !isEnabled(id, chat)
        )
    }

    override suspend fun setStatus(id: FunctionId, chat: Chat, isEnabled: Boolean) {
        TODO("Not going to be implemented, use redis repository for that")
    }
}
