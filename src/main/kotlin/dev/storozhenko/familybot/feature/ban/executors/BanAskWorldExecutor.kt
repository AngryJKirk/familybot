package dev.storozhenko.familybot.feature.ban.executors

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.askworld.models.AskWorldQuestion
import dev.storozhenko.familybot.feature.askworld.repos.AskWorldRepository
import dev.storozhenko.familybot.feature.ban.services.BanService
import dev.storozhenko.familybot.getLogger
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class BanAskWorldExecutor(
    private val askWorldRepository: AskWorldRepository,
    private val banService: BanService,
) : CommandExecutor() {
    private val log = getLogger()
    override fun command() = Command.BAN

    override suspend fun execute(context: ExecutorContext) {
        val message = context.message
        if (message.isReply.not()) return

        val replyToMessage = message.replyToMessage
        val questions = askWorldRepository.getQuestionsFromDate(Instant.now().minus(1, ChronoUnit.DAYS))
            .filter {
                replyToMessage.text.contains(it.message, ignoreCase = true)
            }
        log.info("Trying to ban, questions found: {}", questions)
        when (questions.size) {
            0 -> context.sender.send(context, "Can't find anyone, sorry, my master")
            1 -> ban(context, questions.first())
            else ->
                questions
                    .distinctBy { question -> question.user.id }
                    .forEach { question -> ban(context, question) }
        }
    }

    override fun canExecute(context: ExecutorContext): Boolean {
        return context.isFromDeveloper && super.canExecute(context)
    }

    private suspend fun ban(context: ExecutorContext, question: AskWorldQuestion) {
        val tokens = context.update.message.text.split(" ")
        val banReason = tokens[1]
        val isChat = tokens.getOrNull(2) == "chat"
        if (isChat) {
            banService.banChat(question.chat, banReason)
            context.sender.send(context, "${question.chat} is banned, my master", replyToUpdate = true)
        } else {
            banService.banUser(question.user, banReason)
            context.sender.send(context, "${question.user} is banned, my master", replyToUpdate = true)
        }
    }
}
