package space.yaroslav.familybot.repos.ifaces

import space.yaroslav.familybot.common.AskWorldQuestion
import space.yaroslav.familybot.common.AskWorldReply
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit


interface AskWorldRepository {

    fun addQuestion(question: AskWorldQuestion)

    fun getQuestionsFromDate(date: Instant = Instant.now().minusSeconds(60*60)): List<AskWorldQuestion>

    fun addReply(reply: AskWorldReply): Long

    fun isReplied(askWorldQuestion: AskWorldQuestion, chat: Chat, user: User): Boolean

    fun isQuestionDelivered(question: AskWorldQuestion, chat: Chat): Boolean

    fun isReplyDelivered(reply: AskWorldReply): Boolean

    fun addQuestionDeliver(question: AskWorldQuestion, chat: Chat)

    fun addReplyDeliver(reply: AskWorldReply)

    fun findQuestionByText(message: String, date: Instant): List<AskWorldQuestion>

    fun findQuestionByMessageId(messageId: Long, chat: Chat): AskWorldQuestion

    fun getReplies(askWorldQuestion: AskWorldQuestion): List<AskWorldReply>

    fun getQuestionsFromChat(chat: Chat, date: Instant = Instant.now().minusSeconds(60*60)): List<AskWorldQuestion>

    fun getQuestionsFromUser(chat: Chat, user: User, date: Instant = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant()): List<AskWorldQuestion>

}