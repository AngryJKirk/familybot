package space.yaroslav.familybot.repos

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.repos.ifaces.ChatLogRepository

@Component
class PostgresChatLogRepository(val template: JdbcTemplate) : ChatLogRepository {
    override fun getSingle(chat: Chat): String {
        return template.queryForObject("SELECT message FROM chat_log where chat_id = ${chat.id} ORDER BY RANDOM() LIMIT 1", String::class.java);
    }

    override fun add(user: User, message: String) {
        template.update("INSERT INTO chat_log (chat_id, user_id, message) VALUES (${user.chat.id}, ${user.id}, '${message.replace("'", "")}')")
    }

    override fun get(user: User): List<String> {
        return template.queryForList("SELECT message FROM chat_log where user_id = ${user.id} and chat_id = ${user.chat.id}", String::class.java).toList()
    }
}