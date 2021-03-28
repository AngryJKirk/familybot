package space.yaroslav.familybot.repos

import io.micrometer.core.annotation.Timed
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.CustomMessage
import space.yaroslav.familybot.repos.ifaces.CustomMessageDeliveryRepository

@Component
class PostgresCustomMessageDeliveryRepository(val jdbcTemplate: JdbcTemplate) : CustomMessageDeliveryRepository {
    @Timed("repository.CustomMessageDeliveryRepository.hasNewMessages")
    override fun hasNewMessages(chat: Chat): Boolean {
        return jdbcTemplate.query(
            "SELECT 1 FROM custom_message_delivery where chat_id = ? and is_delivered is false",
            { _, _ -> 1 },
            chat.id
        ).isNotEmpty()
    }

    @Timed("repository.CustomMessageDeliveryRepository.getNewMessages")
    override fun getNewMessages(chat: Chat): List<CustomMessage> {
        return jdbcTemplate.query(
            "SELECT * FROM custom_message_delivery where chat_id = ? and is_delivered is false",
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
    override fun markAsDelivered(message: CustomMessage) {
        jdbcTemplate.update("UPDATE custom_message_delivery set is_delivered = true where id = ?", message.id)
    }
}
