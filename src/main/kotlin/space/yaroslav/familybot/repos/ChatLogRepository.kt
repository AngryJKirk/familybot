package space.yaroslav.familybot.repos

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import io.micrometer.core.annotation.Timed
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.User
import java.util.concurrent.TimeUnit

@Component
class ChatLogRepository(val template: JdbcTemplate) {

    private val defaultBuilder = CacheBuilder
        .newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)

    private val allByUserCache = defaultBuilder.build(
        CacheLoader.from { user: User? ->
            template.queryForList(
                "SELECT message FROM chat_log where user_id = ? and chat_id = ?",
                String::class.java,
                user?.id,
                user?.chat?.id
            ).toList()
        }
    )

    @Timed("repository.ChatLogRepository.getAll")
    fun getAll(): List<String> {
        return template.queryForList(
            "SELECT message FROM chat_log",
            String::class.java
        ).toList().sortedByDescending { it.length }
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
        return allByUserCache[user]
    }
}
