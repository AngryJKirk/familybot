package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.AskWorldQuestion
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.models.Command
import space.yaroslav.familybot.repos.ifaces.AskWorldRepository
import space.yaroslav.familybot.services.ban.Ban
import space.yaroslav.familybot.services.ban.BanService
import space.yaroslav.familybot.telegram.BotConfig
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class BanAskWorldExecutor(
    private val askWorldRepository: AskWorldRepository,
    private val banService: BanService,
    private val botConfig: BotConfig
) : CommandExecutor(botConfig) {
    override fun command() = Command.BAN

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        if (update.message.isReply.not()) return {}

        val replyToMessage = update.message.replyToMessage
        val questions = askWorldRepository.getQuestionsFromDate()
            .filter {
                replyToMessage.text.contains(it.message, ignoreCase = true)
            }
        when (questions.size) {
            0 -> return { it.send(update, "Can't find anyone, sorry, my master") }
            1 -> return ban(update, questions.first())
            else -> return { it.send(update, "Ambiguous choice", replyToUpdate = true) }
        }
    }

    override fun canExecute(message: Message): Boolean {
        return if (super.canExecute(message)) {
            message.from.userName == botConfig.developer
        } else {
            false
        }
    }

    private fun ban(update: Update, question: AskWorldQuestion): suspend (AbsSender) -> Unit {
        val tokens = update.message.text.split(" ")
        val banReason = tokens[1]
        val isChat = tokens.getOrNull(2) == "chat"
        val ban = Ban(description = banReason, till = Instant.now().plus(7, ChronoUnit.DAYS))
        if (isChat) {
            banService.banChat(question.chat, ban)
            return { it.send(update, "${question.chat} is banned, my master", replyToUpdate = true) }
        } else {
            banService.banUser(question.user, ban)
            return {
                it.send(update, "${question.user} is banned, my master", replyToUpdate = true)
            }
        }
    }
}
