package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.AskWorldQuestion
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.AskWorldRepository
import space.yaroslav.familybot.repos.ifaces.HistoryRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.telegram.BotConfig
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Component
class AskWorldInitialExecutor(val askWorldRepository: AskWorldRepository,
                              val historyRepository: HistoryRepository,
                              val botConfig: BotConfig) : CommandExecutor, Configurable {

    override fun getFunctionId(): FunctionId {
        return FunctionId.ASK_WORLD
    }

    private val helpMessage: String = """

        Данная команда позволяет вам задать вопрос всем остальным чатам, где есть этот бот.
        Использование: ${command().command} <вопрос>
        Если вам придет вопрос, то нужно ответить на него, в таком случае ответ отправится в чат, где он был задан.
        Ответить можно лишь один раз от человека.
        Лимиты: не более одного вопроса от человека в день, не более 5 вопросов от чата в день.
        Команда работает в тестовом режиме. В настройках можно отключить ее, тогда вам не будут приходить вопросы и вы сами не сможете их задавать.

    """

    final override fun command(): Command {
        return Command.ASK_WORLD
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val chatId = update.message.chatId
        val message = update.message
                ?.text
                ?.removePrefix(command().command)
                ?.removePrefix("@${botConfig.botname}")
                ?.takeIf { it.isNotEmpty() } ?: return {
            it.execute(SendMessage(chatId,
                    helpMessage))
        }

        val chat = update.toChat()
        val user = update.toUser()

        if (historyRepository.get(user, from = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant()).isNotEmpty()) {
            return { it.execute(SendMessage(chat.id, "Не более вопроса в день от пользователя").setReplyToMessageId(update.message.messageId)) }
        }
        if (historyRepository.getAll(chat).size >= 5) {
            return { it.execute(SendMessage(chat.id, "Не более 5 вопросов в день от чата").setReplyToMessageId(update.message.messageId)) }
        }

        askWorldRepository.addQuestion(
                AskWorldQuestion(null, message, user, chat, Instant.now(), null))
        return { it.execute(SendMessage(chatId, "Принято")) }
    }
}