package space.yaroslav.familybot.repos

import com.google.common.base.Suppliers
import java.util.concurrent.TimeUnit
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.repos.ifaces.ChatLogRepository

@Component
class PostgresChatLogRepository(val template: JdbcTemplate) : ChatLogRepository {
    private val cache = Suppliers.memoizeWithExpiration({
        template.queryForList(
            "SELECT message FROM chat_log",
            String::class.java
        ).toList().sortedByDescending { it.length }
    }, 10, TimeUnit.MINUTES)

    override fun getAllByChat(chat: Chat): List<String> {
        return template.queryForList(
            "SELECT message from chat_log where chat_id = ?",
            String::class.java, chat.id
        )
    }

    override fun getAll(): List<String> {
        return cache.get()
    }

    override fun add(user: User, message: String) {
        template.update(
            "INSERT INTO chat_log (chat_id, user_id, message) VALUES (?, ?, ?)",
            user.chat.id,
            user.id,
            message.replace("'", "")
        )
    }

    override fun get(user: User): List<String> {
        return template.queryForList(
            "SELECT message FROM chat_log where user_id = ? and chat_id = ?",
            String::class.java, user.id, user.chat.id
        ).toList()
    }
}
