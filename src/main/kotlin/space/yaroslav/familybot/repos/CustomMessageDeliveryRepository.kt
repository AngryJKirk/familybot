package space.yaroslav.familybot.repos

import io.micrometer.core.annotation.Timed
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.CustomMessage

@Component
class CustomMessageDeliveryRepository(val jdbcTemplate: JdbcTemplate) {
    @Timed("repository.CustomMessageDeliveryRepository.hasNewMessages")
    fun hasNewMessages(chat: Chat): Boolean {
        return jdbcTemplate.query(
            "SELECT 1 FROM custom_message_delivery WHERE chat_id = ? AND is_delivered IS FALSE",
            { _, _ -> 1 },
            chat.id
        ).isNotEmpty()
    }

    @Timed("repository.CustomMessageDeliveryRepository.getNewMessages")
    fun getNewMessages(chat: Chat): List<CustomMessage> {
        return jdbcTemplate.query(
            "SELECT * FROM custom_message_delivery WHERE chat_id = ? AND is_delivered IS FALSE",
            { rs, _ ->
                CustomMessage(
                    rs.getLong("id"),
                    Chat(rs.getLong("chat_id"), null),
                    rs.getString("message")
                )
            },
            chat.id
        )
    }

    @Timed("repository.CustomMessageDeliveryRepository.markAsDelivered")
    fun markAsDelivered(message: CustomMessage) {
        jdbcTemplate.update("UPDATE custom_message_delivery SET is_delivered = TRUE WHERE id = ?", message.id)
    }
}
