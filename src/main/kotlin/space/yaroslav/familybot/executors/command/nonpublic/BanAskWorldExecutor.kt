package space.yaroslav.familybot.executors.command.nonpublic

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.askworld.AskWorldQuestion
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.repos.AskWorldRepository
import space.yaroslav.familybot.services.misc.BanService
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class BanAskWorldExecutor(
    private val askWorldRepository: AskWorldRepository,
    private val banService: BanService
) : CommandExecutor() {
    private val log = getLogger()
    override fun command() = Command.BAN

    override fun execute(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {
        val message = executorContext.message
        if (message.isReply.not()) return {}

        val replyToMessage = message.replyToMessage
        val questions = askWorldRepository.getQuestionsFromDate(Instant.now().minus(1, ChronoUnit.DAYS))
            .filter {
                replyToMessage.text.contains(it.message, ignoreCase = true)
            }
        log.info("Trying to ban, questions found: {}", questions)
        when (questions.size) {
            0 -> return { it.send(executorContext, "Can't find anyone, sorry, my master") }
            1 -> return ban(executorContext, questions.first())
            else -> return { sender ->
                questions
                    .distinctBy { question -> question.user.id }
                    .map { question -> ban(executorContext, question) }
                    .forEach { it.invoke(sender) }
            }
        }
    }

    override fun canExecute(executorContext: ExecutorContext): Boolean {
        return executorContext.isFromDeveloper && super.canExecute(executorContext)
    }

    private fun ban(executorContext: ExecutorContext, question: AskWorldQuestion): suspend (AbsSender) -> Unit {

        val tokens = executorContext.update.message.text.split(" ")
        val banReason = tokens[1]
        val isChat = tokens.getOrNull(2) == "chat"
        if (isChat) {
            banService.banChat(question.chat, banReason)
            return { it.send(executorContext, "${question.chat} is banned, my master", replyToUpdate = true) }
        } else {
            banService.banUser(question.user, banReason)
            return {
                it.send(executorContext, "${question.user} is banned, my master", replyToUpdate = true)
            }
        }
    }
}
