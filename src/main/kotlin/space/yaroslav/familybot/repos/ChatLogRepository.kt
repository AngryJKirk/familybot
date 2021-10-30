package space.yaroslav.familybot.repos

import com.github.benmanes.caffeine.cache.Caffeine
import io.micrometer.core.annotation.Timed
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.models.telegram.User
import java.time.Duration

@Component
class ChatLogRepository(val template: JdbcTemplate) {

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

    @Timed("repository.ChatLogRepository.getAll")
    fun getAll(): List<String> {
        return template.queryForList(
            "SELECT message FROM chat_log",
            String::class.java
        )
    }

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
}
