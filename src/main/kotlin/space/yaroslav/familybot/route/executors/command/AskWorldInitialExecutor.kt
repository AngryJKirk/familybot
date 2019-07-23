package space.yaroslav.familybot.route.executors.command

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.AskWorldQuestion
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.utils.boldNullable
import space.yaroslav.familybot.common.utils.italic
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.AskWorldRepository
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.repos.ifaces.FunctionsConfigureRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.services.dictionary.Dictionary
import space.yaroslav.familybot.telegram.BotConfig
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Component
class AskWorldInitialExecutor(
    val askWorldRepository: AskWorldRepository,
    val commonRepository: CommonRepository,
    val configureRepository: FunctionsConfigureRepository,
    val botConfig: BotConfig,
    val dictionary: Dictionary
) : CommandExecutor, Configurable {
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

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val chat = update.toChat()
        val message = update.message
            ?.text
            ?.removePrefix(command().command)
            ?.removePrefix("@${botConfig.botname}")
            ?.takeIf(String::isNotEmpty) ?: return { it.send(update, helpMessage) }

        if (isLimitForChatExceed(chat)) {
            return {
                it.send(update, dictionary.get(Phrase.ASK_WORLD_LIMIT_BY_CHAT), replyToUpdate = true)
            }
        }

        if (isLimitForUserExceed(chat, update)) {
            return {
                it.send(update, dictionary.get(Phrase.ASK_WORLD_LIMIT_BY_USER), replyToUpdate = true)
            }
        }

        val question = AskWorldQuestion(null, message, update.toUser(), chat, Instant.now(), null)
        return { sender ->
            val questionId = GlobalScope.async { askWorldRepository.addQuestion(question) }
            sender.send(update, dictionary.get(Phrase.DATA_CONFIRM))
            commonRepository.getChats()
                .filterNot { it == chat }
                .filter(this@AskWorldInitialExecutor::isEnabledInChat)
                .forEach {
                    runCatching {
                        val result = sender.execute(SendMessage(it.id, formatMessage(chat, question)).enableHtml(true))
                        markQuestionDelivered(question, questionId, result, it)
                    }.onFailure { e -> markChatInactive(it, questionId, e) }
                }
        }
    }

    private fun markChatInactive(
        chat: Chat,
        questionId: Deferred<Long>,
        e: Throwable
    ) {
        commonRepository.changeChatActiveStatus(chat, false)
        log.warn("Could not send question $questionId to $chat due to error: [${e.message}]")
    }

    private suspend fun markQuestionDelivered(
        question: AskWorldQuestion,
        questionId: Deferred<Long>,
        result: Message,
        chat: Chat
    ) {
        val questionWithIds = question.copy(
            id = questionId.await(),
            messageId = result.messageId + chat.id
        )
        askWorldRepository.addQuestionDeliver(questionWithIds, chat)
    }

    private fun formatMessage(chat: Chat, question: AskWorldQuestion): String {
        val messagePrefix = dictionary.get(Phrase.ASK_WORLD_QUESTION_FROM_CHAT)
        val boldChatName = chat.name.boldNullable()
        val italicMessage = question.message.italic()
        return "$messagePrefix $boldChatName: $italicMessage"
    }

    private fun isEnabledInChat(it: Chat) = configureRepository.isEnabled(getFunctionId(), it)

    private fun isLimitForChatExceed(chat: Chat): Boolean {
        return askWorldRepository.getQuestionsFromChat(
            chat,
            date = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant()
        ).size >= 2
    }

    private fun isLimitForUserExceed(
        chat: Chat,
        update: Update
    ) = askWorldRepository.getQuestionsFromUser(chat, update.toUser()).isNotEmpty()
}
