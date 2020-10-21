package space.yaroslav.familybot.executors.command

import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.AskWorldQuestion
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.utils.boldNullable
import space.yaroslav.familybot.common.utils.getLogger
import space.yaroslav.familybot.common.utils.italic
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.models.Command
import space.yaroslav.familybot.models.FunctionId
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.repos.ifaces.AskWorldRepository
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.repos.ifaces.FunctionsConfigureRepository
import space.yaroslav.familybot.services.dictionary.Dictionary
import space.yaroslav.familybot.telegram.BotConfig

@Component
class AskWorldInitialExecutor(
    private val askWorldRepository: AskWorldRepository,
    private val commonRepository: CommonRepository,
    private val configureRepository: FunctionsConfigureRepository,
    private val botConfig: BotConfig,
    private val dictionary: Dictionary
) : CommandExecutor(botConfig), Configurable {
    private val log = getLogger()
    override fun getFunctionId(): FunctionId {
        return FunctionId.ASK_WORLD
    }

    final override fun command(): Command {
        return Command.ASK_WORLD
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val chat = update.toChat()
        val message = update.message
            ?.text
            ?.removePrefix(command().command)
            ?.removePrefix("@${botConfig.botname}")
            ?.takeIf(String::isNotEmpty) ?: return { it.send(update, dictionary.get(Phrase.ASK_WORLD_HELP)) }

        if (isLimitForChatExceed(chat)) {
            return {
                log.info("Limit was exceed for chat")
                it.send(update, dictionary.get(Phrase.ASK_WORLD_LIMIT_BY_CHAT), replyToUpdate = true)
            }
        }

        if (isLimitForUserExceed(chat, update)) {
            return {
                log.info("Limit was exceed for user")
                it.send(update, dictionary.get(Phrase.ASK_WORLD_LIMIT_BY_USER), replyToUpdate = true)
            }
        }

        val isScam = containsUrl(message) || isSpam(message)

        if (message.length > 2000) {
            return {
                it.send(
                    update,
                    "Слишком длинный вопрос, иди нахуй",
                    replyToUpdate = true
                )
            } // todo move to dictionary
        }

        val question = AskWorldQuestion(null, message, update.toUser(), chat, Instant.now(), null)
        return { sender ->
            val questionId = GlobalScope.async { askWorldRepository.addQuestion(question) }
            sender.send(update, dictionary.get(Phrase.DATA_CONFIRM))
            getChatsToSendQuestion(chat, isScam)
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
        ).isNotEmpty()
    }

    private fun isLimitForUserExceed(
        chat: Chat,
        update: Update
    ) = askWorldRepository.getQuestionsFromUser(chat, update.toUser()).isNotEmpty()

    private fun getChatsToSendQuestion(currentChat: Chat, isScam: Boolean): List<Chat> {
        if (isScam) {
            log.info("Some scam message was found and it won't be sent")
            return emptyList()
        }

        val allChats = commonRepository.getChats()
            .filterNot { it == currentChat }
            .filter(this@AskWorldInitialExecutor::isEnabledInChat)
            .shuffled()

        return allChats.take(allChats.size / 4)
    }

    private fun containsUrl(message: String): Boolean {
        return message.contains("http", ignoreCase = true)
            || message.contains("www", ignoreCase = true)
            || message.contains("jpg", ignoreCase = true)
            || message.contains("png", ignoreCase = true)
            || message.contains("jpeg", ignoreCase = true)
            || message.contains("bmp", ignoreCase = true)
            || message.contains("gif", ignoreCase = true)
    }

    private fun isSpam(message: String): Boolean {
        return askWorldRepository
            .findQuestionByText(
                message,
                date = Instant.now().minus(30, ChronoUnit.DAYS)
            ).isNotEmpty()
    }
}
