package space.yaroslav.familybot.repos

import com.google.common.base.Suppliers
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import java.util.concurrent.TimeUnit
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.repos.ifaces.ChatLogRepository

@Component
class PostgresChatLogRepository(val template: JdbcTemplate) : ChatLogRepository {
    private val allCache = Suppliers.memoizeWithExpiration({
        template.queryForList(
            "SELECT message FROM chat_log",
            String::class.java
        ).toList().sortedByDescending { it.length }
    }, 10, TimeUnit.MINUTES)

    private val defaultBuilder = CacheBuilder
        .newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)

    private val allByChatCache = defaultBuilder.build(
        CacheLoader.from { chat: Chat? ->
            template.queryForList(
                "SELECT message from chat_log where chat_id = ?",
                String::class.java, chat?.id
            )
        }
    )

    private val allByUserCache = defaultBuilder.build(
        CacheLoader.from { user: User? ->
            template.queryForList(
                "SELECT message FROM chat_log where user_id = ? and chat_id = ?",
                String::class.java, user?.id, user?.chat?.id
            ).toList()
        }
    )

    override fun getAllByChat(chat: Chat): List<String> {
        return allByChatCache[chat]
    }

    override fun getAll(): List<String> {
        return allCache.get()
    }

    override fun add(user: User, message: String) {
        template.update(
            "INSERT INTO chat_log (chat_id, user_id, message) VALUES (?, ?, ?)",
            user.chat.id,
            user.id,
            message
        )
    }

    override fun get(user: User): List<String> {
        return allByUserCache[user]
    }
}
