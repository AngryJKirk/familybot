package space.yaroslav.familybot.route.executors.command

import kotlinx.coroutines.experimental.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.AskWorldQuestion
import space.yaroslav.familybot.common.utils.bold
import space.yaroslav.familybot.common.utils.italic
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.AskWorldRepository
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.repos.ifaces.FunctionsConfigureRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.telegram.BotConfig
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Component
class AskWorldInitialExecutor(val askWorldRepository: AskWorldRepository,
                              val commonRepository: CommonRepository,
                              val configureRepository: FunctionsConfigureRepository,
                              val botConfig: BotConfig) : CommandExecutor, Configurable {
    private val log = LoggerFactory.getLogger(AskWorldInitialExecutor::class.java)
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
        if (askWorldRepository.getQuestionsFromChat(chat, date = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant()).size >= 5) {
            return { it.execute(SendMessage(chat.id, "Не более 5 вопросов в день от чата").setReplyToMessageId(update.message.messageId)) }
        }

        if (askWorldRepository.getQuestionsFromUser(chat, update.toUser()).isNotEmpty()) {
            return { it.execute(SendMessage(chat.id, "Не более вопроса в день от пользователя").setReplyToMessageId(update.message.messageId)) }
        }

        val question = AskWorldQuestion(null, message, update.toUser(), chat, Instant.now(), null)
        val id = askWorldRepository.addQuestion(question)
        return {sender ->
            sender.execute(SendMessage(chatId, "Принято"))
            commonRepository.getChats()
                    .filterNot { it == chat }
                    .filter { configureRepository.isEnabled(getFunctionId(), it) }
                    .forEach {
                try {
                    val result = sender.execute(SendMessage(it.id, "Вопрос из чата ${chat.name.bold()}: ${question.message.italic()}")
                            .enableHtml(true))
                    launch { askWorldRepository.addQuestionDeliver(question.copy(id = id, messageId = result.messageId + it.id), it) }
                } catch (e: Exception) {
                    log.warn("Could not send question $id to $it")
                }
            }
        }
    }
}