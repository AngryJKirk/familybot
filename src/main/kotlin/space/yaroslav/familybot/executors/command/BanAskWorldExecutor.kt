package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.askworld.AskWorldQuestion
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.repos.AskWorldRepository
import space.yaroslav.familybot.services.misc.BanService
import space.yaroslav.familybot.telegram.BotConfig
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class BanAskWorldExecutor(
    private val askWorldRepository: AskWorldRepository,
    private val banService: BanService,
    private val botConfig: BotConfig
) : CommandExecutor(botConfig) {
    private val log = getLogger()
    override fun command() = Command.BAN

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        if (update.message.isReply.not()) return {}

        val replyToMessage = update.message.replyToMessage
        val questions = askWorldRepository.getQuestionsFromDate(Instant.now().minus(1, ChronoUnit.DAYS))
            .filter {
                replyToMessage.text.contains(it.message, ignoreCase = true)
            }
        log.info("Trying to ban, questions found: {}", questions)
        when (questions.size) {
            0 -> return { it.send(update, "Can't find anyone, sorry, my master") }
            1 -> return ban(update, questions.first())
            else -> return { sender ->
                questions
                    .distinctBy { question -> question.user.id }
                    .map { question -> ban(update, question) }
                    .forEach { it.invoke(sender) }
            }
        }
    }

    override fun canExecute(message: Message): Boolean {
        return message.from.userName == botConfig.developer && super.canExecute(message)
    }

    private fun ban(update: Update, question: AskWorldQuestion): suspend (AbsSender) -> Unit {

        val tokens = update.message.text.split(" ")
        val banReason = tokens[1]
        val isChat = tokens.getOrNull(2) == "chat"
        if (isChat) {
            banService.banChat(question.chat, banReason)
            return { it.send(update, "${question.chat} is banned, my master", replyToUpdate = true) }
        } else {
            banService.banUser(question.user, banReason)
            return {
                it.send(update, "${question.user} is banned, my master", replyToUpdate = true)
            }
        }
    }
}
