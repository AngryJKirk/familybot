package dev.storozhenko.familybot.repos

import io.micrometer.core.annotation.Timed
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import dev.storozhenko.familybot.common.extensions.toAskWorldQuestion
import dev.storozhenko.familybot.models.askworld.AskWorldQuestion
import dev.storozhenko.familybot.models.askworld.AskWorldReply
import dev.storozhenko.familybot.models.telegram.Chat
import dev.storozhenko.familybot.models.telegram.User
import dev.storozhenko.familybot.telegram.FamilyBot
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
                          c2.name as chat_name,
                          u.name as common_name,
                          u.username
                            from ask_world_questions
                            INNER JOIN chats c2 on ask_world_questions.chat_id = c2.id
                            INNER JOIN users u on ask_world_questions.user_id = u.id
                            where ask_world_questions.id =
            (SELECT ask_world_questions_delivery.id
            from ask_world_questions_delivery where message_id = ? and chat_id = ?)""",
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
                          c2.name as chat_name,
                          u.name as common_name,
                          u.username
                            from ask_world_questions
                            INNER JOIN chats c2 on ask_world_questions.chat_id = c2.id
                            INNER JOIN users u on ask_world_questions.user_id = u.id
                where date >= ? and question = ?""",
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
                          c2.name as chat_name,
                          u.name as common_name,
                          u.username
                            from ask_world_questions
                            INNER JOIN chats c2 on ask_world_questions.chat_id = c2.id
                            INNER JOIN users u on ask_world_questions.user_id = u.id
                where chat_id = ? and lower(question) like ?""",
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
            "INSERT into ask_world_questions (question, chat_id, user_id, date) VALUES (?, ?, ?, ?) returning id",
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
                          c2.name as chat_name,
                          u.name as common_name,
                          u.username
                            from ask_world_questions
                            INNER JOIN chats c2 on ask_world_questions.chat_id = c2.id
                            INNER JOIN users u on ask_world_questions.user_id = u.id
                where date >= ?""",
            { rs, _ -> rs.toAskWorldQuestion() },
            Timestamp.from(date)
        )
    }

    @Timed("repository.AskWorldRepository.addReply")
    fun addReply(reply: AskWorldReply): Long {
        return template.queryForObject(
            "INSERT into ask_world_replies (question_id, reply, chat_id, user_id, date) VALUES (?, ?, ?, ?, ?) returning id",
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
