package dev.storozhenko.familybot.feature.talking.repos

import com.aallam.openai.api.embedding.Embedding
import com.pgvector.PGvector
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.talking.models.Kind
import dev.storozhenko.familybot.feature.talking.models.RagHit
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.time.Instant
import kotlin.math.exp

@Component
class RagRepository(private val template: JdbcTemplate) {


    fun add(executorContext: ExecutorContext, embedding: Embedding, textOverride: String? = null) {

        template.update(
            """
            INSERT INTO rag_msg_index (chat_id, msg_id, ts, user_id, text, embedding)
            VALUES (?, ?, ?, ?, ?, ?)            
        """.trimIndent(),
            executorContext.chat.id,
            executorContext.message.messageId,
            Timestamp.from(Instant.now()),
            executorContext.user.id,
            textOverride ?: executorContext.message.text,
            PGvector(embedding.embedding)
        )

    }


    fun searchSemantic(
        executorContext: ExecutorContext,
        queryEmbedding: Embedding,
        limit: Int = 20,
    ): List<RagHit> {
        val vec = PGvector(queryEmbedding.embedding)
        val sql = """
            SELECT rag_id, msg_id, user_id, ts, text, (embedding <=> ?) AS dist
            FROM rag_msg_index
            WHERE chat_id = ? AND embedding IS NOT NULL
            ORDER BY embedding <=> ?
            LIMIT ?
        """.trimIndent()
        return template.query(
            sql,
            { rs, _ ->
                val dist = rs.getDouble("dist")
                RagHit(
                    ragId = rs.getLong("rag_id"),
                    msgId = rs.getLong("msg_id"),
                    userId = rs.getLong("user_id"),
                    ts = rs.getTimestamp("ts").toInstant(),
                    text = rs.getString("text"),
                    score = 1.0 - dist,
                    kind = Kind.SEMANTIC
                )
            },
            vec,
            executorContext.chat.id,
            vec,
            limit
        )
    }

    fun searchKeywordRu(
        executorContext: ExecutorContext,
        q: String,
        limit: Int = 20,
    ): List<RagHit> {
        val sql = """
            SELECT rag_id, msg_id, user_id, ts, text,
                   ts_rank(tsv, plainto_tsquery('russian', ?)) AS kw_score
            FROM rag_msg_index
            WHERE chat_id = ? AND tsv @@ plainto_tsquery('russian', ?)
            ORDER BY kw_score DESC
            LIMIT ?
        """.trimIndent()
        return template.query(
            sql,
            { rs, _ ->
                RagHit(
                    ragId = rs.getLong("rag_id"),
                    msgId = rs.getLong("msg_id"),
                    userId = rs.getLong("user_id"),
                    ts = rs.getTimestamp("ts").toInstant(),
                    text = rs.getString("text"),
                    score = rs.getDouble("kw_score"),
                    kind = Kind.KEYWORD_RU
                )
            },
            q, executorContext.chat.id, q, limit
        )
    }

    fun searchKeywordSimple(
        executorContext: ExecutorContext,
        q: String,
        limit: Int = 20,
    ): List<RagHit> {
        val sql = """
            SELECT rag_id, msg_id, user_id, ts, text,
                   ts_rank(to_tsvector('simple', coalesce(text,'')),
                           plainto_tsquery('simple', ?)) AS kw_score
            FROM rag_msg_index
            WHERE chat_id = ?
              AND to_tsvector('simple', coalesce(text,'')) @@ plainto_tsquery('simple', ?)
            ORDER BY kw_score DESC
            LIMIT ?
        """.trimIndent()
        return template.query(
            sql,
            { rs, _ ->
                RagHit(
                    ragId = rs.getLong("rag_id"),
                    msgId = rs.getLong("msg_id"),
                    userId = rs.getLong("user_id"),
                    ts = rs.getTimestamp("ts").toInstant(),
                    text = rs.getString("text"),
                    score = rs.getDouble("kw_score"),
                    kind = Kind.KEYWORD_SIMPLE
                )
            },
            q, executorContext.chat.id, q, limit
        )
    }

    fun recentWindow(
        executorContext: ExecutorContext,
        minutes: Long = 30,
        limit: Int = 40,
    ): List<RagHit> {
        val sql = """
            SELECT rag_id, msg_id, user_id, ts, text
            FROM rag_msg_index
            WHERE chat_id = ?
              AND ts > now() - (? || ' minutes')::interval
            ORDER BY ts DESC
            LIMIT ?
        """.trimIndent()
        val now = Instant.now()
        return template.query(
            sql,
            { rs, _ ->
                val ts = rs.getTimestamp("ts").toInstant()
                val minutesSince = (now.epochSecond - ts.epochSecond) / 60.0
                val rec = exp(-minutesSince / 720.0)
                RagHit(
                    ragId = rs.getLong("rag_id"),
                    msgId = rs.getLong("msg_id"),
                    userId = rs.getLong("user_id"),
                    ts = ts,
                    text = rs.getString("text"),
                    score = rec,
                    kind = Kind.RECENT
                )
            },
            executorContext.chat.id, minutes.toString(), limit
        )
    }

    fun searchFuzzy(
        executorContext: ExecutorContext,
        q: String,
        limit: Int = 10,
    ): List<RagHit> {
        val sql = """
            SELECT rag_id, msg_id, user_id, ts, text, similarity(text, ?) AS sim
            FROM rag_msg_index
            WHERE chat_id = ? AND text % ?
            ORDER BY sim DESC
            LIMIT ?
        """.trimIndent()
        return template.query(
            sql,
            { rs, _ ->
                RagHit(
                    ragId = rs.getLong("rag_id"),
                    msgId = rs.getLong("msg_id"),
                    userId = rs.getLong("user_id"),
                    ts = rs.getTimestamp("ts").toInstant(),
                    text = rs.getString("text"),
                    score = rs.getDouble("sim"),
                    kind = Kind.FUZZY
                )
            },
            q, executorContext.chat.id, q, limit
        )
    }


}