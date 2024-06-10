package dev.storozhenko.familybot.feature.reactions

import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.models.telegram.User
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.reactions.MessageReactionUpdated
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionType
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionTypeCustomEmoji
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionTypeEmoji
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID
import kotlin.time.Duration

@Component
class ReactionRepository(private val jdbcTemplate: JdbcTemplate) {

    fun add(reaction: MessageReactionUpdated) {
        val sql = """
            INSERT INTO reactions (chat_id, from_user_id, message_id, update_time, reactions) 
            VALUES (?, ?, ?, ?, ?) 
            ON CONFLICT (chat_id, from_user_id, message_id) DO UPDATE SET update_time = EXCLUDED.update_time, reactions = EXCLUDED.reactions
        """
        jdbcTemplate.update(
            sql,
            reaction.chat.id,
            reaction.user.id,
            reaction.messageId,
            Timestamp.from(Instant.ofEpochSecond(reaction.date.toLong())),
            parseReactions(reaction.newReaction)
        )
    }

    data class Reaction(
        val from: User,
        val to: User,
        val messageId: Long,
        val reactions: List<String>
    )

    fun get(chat: Chat, reactionsWindow: Duration): List<Reaction> {
        val sql = """
            select u.id as from_id, u.name as from_name, u.username as from_username,
             CONCAT_WS(' ', l.raw_update -> 'message' -> 'from' ->> 'first_name', l.raw_update -> 'message' -> 'from' ->> 'last_name') AS to_name,
             l.raw_update -> 'message' -> 'from' ->> 'username' as to_username,
             l.raw_update -> 'message' -> 'from' ->> 'id' as to_id,
             r.message_id as message_id,
             r.reactions as reactions
            from reactions r
                     inner join users u on u.id = r.from_user_id
                     inner join raw_chat_log l on l.chat_id = r.chat_id AND (l.raw_update -> 'message' ->> 'message_id')::bigint = r.message_id
            where r.chat_id = ? and l.date > ?
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            { rs, _ -> mapReaction(rs, chat) },
            chat.id,
            Timestamp.from(Instant.now().minusSeconds(reactionsWindow.inWholeSeconds))
        )
    }

    private fun mapReaction(rs: ResultSet, chat: Chat): Reaction {
        @Suppress("UNCHECKED_CAST")
        return Reaction(
            from = User(
                rs.getLong("from_id"),
                chat,
                rs.getString("from_name"),
                rs.getString("from_username"),
            ),
            to = User(
                rs.getLong("to_id"),
                chat,
                rs.getString("to_name"),
                rs.getString("to_username"),
            ),
            rs.getLong("message_id"),
            (rs.getArray("reactions").array as Array<String>).toList()
        )
    }

    private fun parseReactions(reactions: List<ReactionType>): Array<String> {
        return reactions.map { reaction ->
            return@map when (reaction) {
                is ReactionTypeEmoji -> {
                    reaction.emoji
                }

                is ReactionTypeCustomEmoji -> {
                    @Suppress("USELESS_ELVIS") // it actually could be null, @NonNull annotation is a bug
                    reaction.customEmojiId ?: UUID.randomUUID().toString()
                }

                else -> {
                    UUID.randomUUID().toString()
                }
            }
        }.toTypedArray()
    }
}