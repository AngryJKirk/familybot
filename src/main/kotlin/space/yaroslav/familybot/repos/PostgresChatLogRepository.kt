package space.yaroslav.familybot.repos

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.repos.ifaces.ChatLogRepository

@Component
class PostgresChatLogRepository(val template: JdbcTemplate) : ChatLogRepository {
    override fun add(user: User, message: String) {
        template.update("INSERT INTO chat_log (chat_id, user_id, message) VALUES (${user.id}, ${user.chat.id}, '${message.replace("'", "")}')")
    }

    override fun get(user: User): List<String> {
        return template.queryForList("SELECT message FROM chat_log where user_id = ${user.id} and chat_id = 1094220065", String::class.java).toList()
    }
}