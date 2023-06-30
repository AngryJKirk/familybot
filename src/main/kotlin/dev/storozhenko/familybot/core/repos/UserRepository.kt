package dev.storozhenko.familybot.core.repos

import dev.storozhenko.familybot.common.extensions.toChat
import dev.storozhenko.familybot.common.extensions.toUser
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.models.telegram.User
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class UserRepository(private val template: JdbcTemplate) {

    fun addUser(user: User) {
        template.update(
            "INSERT INTO users (id, name, username) VALUES (?, ?, ?) ON CONFLICT(id) DO UPDATE SET name = excluded.name, username = excluded.username ",
            user.id,
            user.name,
            user.nickname,
        )
        template.update(
            "INSERT INTO users2chats (chat_id, user_id) VALUES (?, ?) ON CONFLICT(chat_id, user_id) DO UPDATE SET active = TRUE ",
            user.chat.id,
            user.id,
        )
    }

    fun getUsers(chat: Chat, activeOnly: Boolean = false): List<User> {
        var select = "SELECT * FROM users INNER JOIN users2chats u ON users.id = u.user_id WHERE u.chat_id = ${chat.id}"
        if (activeOnly) {
            select += " and u.active = true"
        }
        return template.query(select) { rs, _ -> rs.toUser() }
    }

    fun addChat(chat: Chat) {
        template.update(
            "INSERT INTO chats (id, name) VALUES (?, ?) ON CONFLICT(id) DO UPDATE SET name = excluded.name, active = TRUE",
            chat.id,
            chat.name
                ?: "",
        )
    }

    fun getChats(): List<Chat> {
        return template.query("SELECT * FROM chats WHERE active = TRUE ") { rs, _ -> rs.toChat() }
    }

    fun getChatsAll(): List<Chat> {
        return template.query("SELECT * FROM chats") { rs, _ -> rs.toChat() }
    }

    fun changeChatActiveStatus(chat: Chat, status: Boolean) {
        template.update("UPDATE chats SET active = ? WHERE id = ?", status, chat.id)
    }

    fun changeUserActiveStatusNew(user: User, status: Boolean) {
        template.update(
            "UPDATE users2chats SET active = ? WHERE chat_id = ? AND user_id = ?",
            status,
            user.chat.id,
            user.id,
        )
    }

    fun disableUsersInChat(chat: Chat) {
        template.update("UPDATE users2chats SET active = FALSE WHERE chat_id = ?", chat.id)
    }

    fun findUsersByName(namePart: String): List<User> {
        return template.query(
            "SELECT * FROM users INNER JOIN users2chats u2c ON users.id = u2c.user_id WHERE LOWER(name) LIKE LOWER(?) OR LOWER(username) LIKE LOWER(?)",
            { rs, _ -> rs.toUser() },
            "%$namePart%",
            "%$namePart%",
        )
    }

    fun getChatsByUser(user: User): List<Chat> {
        return template.query(
            "SELECT * FROM chats INNER JOIN users2chats u2c ON chats.id = u2c.chat_id WHERE u2c.user_id = ? ",
            { rs, _ -> rs.toChat() },
            user.id,
        )
    }
}
