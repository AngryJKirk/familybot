package space.yaroslav.familybot.repos

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.AskWorldQuestion
import space.yaroslav.familybot.common.AskWorldReply
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.toAskWorldQuestion
import space.yaroslav.familybot.common.utils.toAskWorldReply
import space.yaroslav.familybot.repos.ifaces.AskWorldRepository
import java.sql.Timestamp
import java.time.Instant

@Component
class PostgresAskWorldRepository(val template: JdbcTemplate) : AskWorldRepository {
    override fun getQuestionsFromUser(chat: Chat, user: User, date: Instant): List<AskWorldQuestion> {
        return template.query("""SELECT
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
                            where ask_world_questions.chat_id = ? and ask_world_questions.user_id = ?
                            and ask_world_questions.date >= ?""", RowMapper { rs, _ -> rs.toAskWorldQuestion() },
                chat.id,
                user.id,
                Timestamp.from(date))
    }

    override fun getQuestionsFromChat(chat: Chat, date: Instant): List<AskWorldQuestion> {
        return template.query("""SELECT
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
                            where ask_world_questions.chat_id = ? and date >= ?""", RowMapper { rs, _ -> rs.toAskWorldQuestion() },
                chat.id,
                Timestamp.from(date))
    }

    override fun getReplies(askWorldQuestion: AskWorldQuestion): List<AskWorldReply> {
        return template.query("""SELECT
                          ask_world_replies.id,
                          ask_world_replies.reply,
                          ask_world_replies.chat_id,
                          ask_world_replies.user_id,
                          ask_world_replies.date,
                          ask_world_replies.question_id,
                          c2.name as chat_name,
                          u.name as common_name,
                          u.username
                            from ask_world_replies
                            INNER JOIN chats c2 on ask_world_replies.chat_id = c2.id
                            INNER JOIN users u on ask_world_replies.user_id = u.id where question_id = ?""",
                RowMapper { rs, _ -> rs.toAskWorldReply() }, askWorldQuestion.id)
    }

    override fun findQuestionByMessageId(messageId: Int, chat: Chat): AskWorldQuestion {
        return template.query("""SELECT
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
                RowMapper { rs, _ -> rs.toAskWorldQuestion() },
                messageId, chat.id).first()
    }

    override fun findQuestionByText(message: String, date: Instant): List<AskWorldQuestion> {
        return template.query("""SELECT
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
                where date >= ? and question = ?""", RowMapper { rs, _ -> rs.toAskWorldQuestion() },
                Timestamp.from(date),
                message)
    }

    override fun addReplyDeliver(reply: AskWorldReply) {
        template.update("INSERT INTO ask_world_replies_delivery (id) values (?)", reply.id)
    }

    override fun addQuestionDeliver(question: AskWorldQuestion, chat: Chat) {
        template.update("INSERT INTO ask_world_questions_delivery (id, chat_id, message_id) VALUES (?, ?, ?)",
                question.id, chat.id, question.messageId)
    }

    override fun isQuestionDelivered(question: AskWorldQuestion, chat: Chat): Boolean {
        return template.queryForList("SELECT 1 from ask_world_questions_delivery where id = ? and chat_id = ?",
                question.id!!, chat.id).isNotEmpty()
    }

    override fun isReplyDelivered(reply: AskWorldReply): Boolean {
        return template.queryForList("SELECT 1 from ask_world_replies_delivery where id = ?",
                reply.id).isNotEmpty()
    }

    override fun addQuestion(question: AskWorldQuestion) {
        template.update("INSERT into ask_world_questions (question, chat_id, user_id, date) VALUES (?, ?, ?, ?)",
                question.message, question.chat.id, question.user.id, Timestamp.from(question.date))
    }

    override fun getQuestionsFromDate(date: Instant): List<AskWorldQuestion> {
        return template.query("""SELECT
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
                RowMapper { rs, _ -> rs.toAskWorldQuestion() },
                Timestamp.from(date))
    }

    override fun addReply(reply: AskWorldReply) {
        template.update("INSERT into ask_world_replies (question_id, reply, chat_id, user_id, date) VALUES (?, ?, ?, ?, ?)",
                reply.questionId, reply.message, reply.chat.id, reply.user.id, Timestamp.from(reply.date))
    }

    override fun isReplied(askWorldQuestion: AskWorldQuestion, chat: Chat, user: User): Boolean {
        return template.queryForList("select 1 from ask_world_replies where question_id = ? and chat_id = ? and user_id =?",
                askWorldQuestion.id, chat.id, user.id)
                .isNotEmpty()
    }
}