package dev.storozhenko.familybot.repos

import com.github.benmanes.caffeine.cache.Caffeine
import io.micrometer.core.annotation.Timed
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import dev.storozhenko.familybot.common.extensions.randomLong
import dev.storozhenko.familybot.models.telegram.User
import java.time.Duration

@Component
class ChatLogRepository(private val template: JdbcTemplate) {

    private val namedJdbcTemplate = NamedParameterJdbcTemplate(template)

    private val allByUserLoader = { (userId, chat): User ->
        template.queryForList(
            "SELECT message FROM chat_log WHERE user_id = ? AND chat_id = ?",
            String::class.java,
            userId,
            chat.id
        )
    }

    private val allByUserCache = Caffeine
        .newBuilder()
        .expireAfterWrite(Duration.ofDays(1))
        .build(allByUserLoader)

    private val commonPoolMaxId: Long = getMaxCommonMessageId()

    @Timed("repository.ChatLogRepository.add")
    fun add(user: User, message: String) {
        template.update(
            "INSERT INTO chat_log (chat_id, user_id, message) VALUES (?, ?, ?)",
            user.chat.id,
            user.id,
            message
        )
    }

    @Timed("repository.ChatLogRepository.get")
    fun get(user: User): List<String> {
        return allByUserCache[user] ?: allByUserLoader(user)
    }

    @Timed("repository.ChatLogRepository.getRandomMessagesFromCommonPool")
    fun getRandomMessagesFromCommonPool(): List<String> {
        if (commonPoolMaxId <= 1) {
            return listOf("хуй соси губой тряси")
        }
        val ids = (1..10).map { randomLong(1, commonPoolMaxId) }.toSet()
        val paramMap = mapOf("ids" to ids)
        return namedJdbcTemplate.queryForList(
            "SELECT message FROM chat_log WHERE id IN (:ids)",
            paramMap,
            String::class.java
        )
    }

    private fun getMaxCommonMessageId(): Long {
        return template.queryForList(
            "SELECT MAX(id) FROM chat_log",
            Long::class.java
        ).firstOrNull() ?: 0
    }
}
