package space.yaroslav.familybot.repos.ifaces

import io.micrometer.core.annotation.Timed
import space.yaroslav.familybot.common.AskWorldQuestion
import space.yaroslav.familybot.common.AskWorldReply
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit


interface AskWorldRepository {

    @Timed("AskWorldRepository.addQuestion")
    fun addQuestion(question: AskWorldQuestion): Long

    @Timed("AskWorldRepository.getQuestionsFromDate")
    fun getQuestionsFromDate(date: Instant = Instant.now().minusSeconds(60 * 60 * 24)): List<AskWorldQuestion>

    @Timed("AskWorldRepository.addReply")
    fun addReply(reply: AskWorldReply): Long

    @Timed("AskWorldRepository.isReplied")
    fun isReplied(askWorldQuestion: AskWorldQuestion, chat: Chat, user: User): Boolean

    @Timed("AskWorldRepository.isQuestionDelivered")
    fun isQuestionDelivered(question: AskWorldQuestion, chat: Chat): Boolean

    @Timed("AskWorldRepository.isReplyDelivered")
    fun isReplyDelivered(reply: AskWorldReply): Boolean

    @Timed("AskWorldRepository.addQuestionDeliver")
    fun addQuestionDeliver(question: AskWorldQuestion, chat: Chat)

    @Timed("AskWorldRepository.addReplyDeliver")
    fun addReplyDeliver(reply: AskWorldReply)

    @Timed("AskWorldRepository.findQuestionByText")
    fun findQuestionByText(message: String, date: Instant): List<AskWorldQuestion>

    @Timed("AskWorldRepository.findQuestionByMessageId")
    fun findQuestionByMessageId(messageId: Long, chat: Chat): AskWorldQuestion

    @Timed("AskWorldRepository.getReplies")
    fun getReplies(askWorldQuestion: AskWorldQuestion): List<AskWorldReply>

    @Timed("AskWorldRepository.getQuestionsFromChat")
    fun getQuestionsFromChat(chat: Chat, date: Instant = Instant.now().minusSeconds(60 * 60)): List<AskWorldQuestion>

    @Timed("AskWorldRepository.getQuestionsFromUser")
    fun getQuestionsFromUser(
        chat: Chat,
        user: User,
        date: Instant = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant()
    ): List<AskWorldQuestion>

    @Timed("AskWorldRepository.getQuestionFromUserAllChats")
    fun getQuestionFromUserAllChats(
        user: User,
        date: Instant = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant()
    ): List<AskWorldQuestion>
}
