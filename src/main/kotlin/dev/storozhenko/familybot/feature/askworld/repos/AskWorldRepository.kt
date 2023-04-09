package dev.storozhenko.familybot.feature.askworld.repos

import dev.storozhenko.familybot.common.extensions.toAskWorldQuestion
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.models.telegram.User
import dev.storozhenko.familybot.core.telegram.FamilyBot
import dev.storozhenko.familybot.feature.askworld.models.AskWorldQuestion
import dev.storozhenko.familybot.feature.askworld.models.AskWorldReply
import io.micrometer.core.annotation.Timed
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.time.Instant

@Component
class AskWorldRepository(private val template: JdbcTemplate) {

    @Timed("repository.AskWorldRepository.findQuestionByMessageId")
    fun findQuestionByMessageId(messageId: Long, chatId: Long): AskWorldQuestion? {
        return template.query(
            """SELECT
                          ask_world_questions.id,
                          ask_world_questions.question,
                          ask_world_questions.chat_id,
                          ask_world_questions.user_id,
                          ask_world_questions.date,
                          c2.name AS chat_name,
                          u.name AS common_name,
                          u.username
                            FROM ask_world_questions
                            INNER JOIN chats c2 ON ask_world_questions.chat_id = c2.id
                            INNER JOIN users u ON ask_world_questions.user_id = u.id
                            WHERE ask_world_questions.id =
            (SELECT ask_world_questions_delivery.id
            FROM ask_world_questions_delivery WHERE message_id = ? AND chat_id = ?)""",
            { rs, _ -> rs.toAskWorldQuestion() },
            messageId,
            chatId
        ).firstOrNull()
    }

    @Timed("repository.AskWorldRepository.findQuestionByText")
    fun findQuestionByText(message: String, date: Instant): List<AskWorldQuestion> {
        return template.query(
            """SELECT
                          ask_world_questions.id,
                          ask_world_questions.question,
                          ask_world_questions.chat_id,
                          ask_world_questions.user_id,
                          ask_world_questions.date,
                          c2.name AS chat_name,
                          u.name AS common_name,
                          u.username
                            FROM ask_world_questions
                            INNER JOIN chats c2 ON ask_world_questions.chat_id = c2.id
                            INNER JOIN users u ON ask_world_questions.user_id = u.id
                WHERE date >= ? AND question = ?""",
            { rs, _ -> rs.toAskWorldQuestion() },
            Timestamp.from(date),
            message
        )
    }

    @Timed("repository.AskWorldRepository.searchQuestion")
    fun searchQuestion(message: String, chat: Chat): List<AskWorldQuestion> {
        return template.query(
            """SELECT
                          ask_world_questions.id,
                          ask_world_questions.question,
                          ask_world_questions.chat_id,
                          ask_world_questions.user_id,
                          ask_world_questions.date,
                          c2.name AS chat_name,
                          u.name AS common_name,
                          u.username
                            FROM ask_world_questions
                            INNER JOIN chats c2 ON ask_world_questions.chat_id = c2.id
                            INNER JOIN users u ON ask_world_questions.user_id = u.id
                WHERE chat_id = ? AND LOWER(question) LIKE ?""",
            { rs, _ -> rs.toAskWorldQuestion() },
            chat.id,
            "%${message.lowercase()}%"
        )
    }

    @Timed("repository.AskWorldRepository.addQuestionDeliver")
    fun addQuestionDeliver(question: AskWorldQuestion, chat: Chat) {
        template.update(
            "INSERT INTO ask_world_questions_delivery (id, chat_id, message_id) VALUES (?, ?, ?)",
            question.id,
            chat.id,
            question.messageId
        )
    }

    @Timed("repository.AskWorldRepository.addQuestion")
    fun addQuestion(question: AskWorldQuestion): Long {
        return template.queryForObject(
            "INSERT INTO ask_world_questions (question, chat_id, user_id, date) VALUES (?, ?, ?, ?) RETURNING id",
            { rs, _ -> rs.getLong("id") },
            question.message,
            question.chat.id,
            question.user.id,
            Timestamp.from(question.date)
        ) ?: throw FamilyBot.InternalException("Something has gone wrong, investigate please")
    }

    @Timed("repository.AskWorldRepository.getQuestionsFromDate")
    fun getQuestionsFromDate(
        date: Instant
    ): List<AskWorldQuestion> {
        return template.query(
            """SELECT
                          ask_world_questions.id,
                          ask_world_questions.question,
                          ask_world_questions.chat_id,
                          ask_world_questions.user_id,
                          ask_world_questions.date,
                          c2.name AS chat_name,
                          u.name AS common_name,
                          u.username
                            FROM ask_world_questions
                            INNER JOIN chats c2 ON ask_world_questions.chat_id = c2.id
                            INNER JOIN users u ON ask_world_questions.user_id = u.id
                WHERE date >= ?""",
            { rs, _ -> rs.toAskWorldQuestion() },
            Timestamp.from(date)
        )
    }

    @Timed("repository.AskWorldRepository.addReply")
    fun addReply(reply: AskWorldReply): Long {
        return template.queryForObject(
            "INSERT INTO ask_world_replies (question_id, reply, chat_id, user_id, date) VALUES (?, ?, ?, ?, ?) RETURNING id",
            { rs, _ -> rs.getLong("id") },
            reply.questionId,
            reply.message,
            reply.chat.id,
            reply.user.id,
            Timestamp.from(reply.date)
        ) ?: throw FamilyBot.InternalException("Something has gone wrong, investigate please")
    }

    @Timed("repository.AskWorldRepository.isReplied")
    fun isReplied(
        askWorldQuestion: AskWorldQuestion,
        chat: Chat,
        user: User
    ): Boolean {
        return template.queryForList(
            "SELECT 1 FROM ask_world_replies WHERE question_id = ? AND chat_id = ? AND user_id =?",
            askWorldQuestion.id,
            chat.id,
            user.id
        )
            .isNotEmpty()
    }
}
