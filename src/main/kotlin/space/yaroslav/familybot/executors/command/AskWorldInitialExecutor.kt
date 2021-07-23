package space.yaroslav.familybot.executors.command

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ForwardMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.boldNullable
import space.yaroslav.familybot.common.extensions.italic
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.toChat
import space.yaroslav.familybot.common.extensions.toUser
import space.yaroslav.familybot.common.extensions.untilNextDay
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.executors.command.settings.AskWorldDensityValue
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.askworld.AskWorldQuestion
import space.yaroslav.familybot.models.askworld.AskWorldQuestionData
import space.yaroslav.familybot.models.askworld.Success
import space.yaroslav.familybot.models.askworld.ValidationError
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.FunctionId
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.repos.AskWorldRepository
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.repos.FunctionsConfigureRepository
import space.yaroslav.familybot.services.settings.AskWorldChatUsages
import space.yaroslav.familybot.services.settings.AskWorldDensity
import space.yaroslav.familybot.services.settings.AskWorldUserUsages
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.services.talking.DictionaryContext
import space.yaroslav.familybot.telegram.BotConfig
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class AskWorldInitialExecutor(
    private val askWorldRepository: AskWorldRepository,
    private val commonRepository: CommonRepository,
    private val configureRepository: FunctionsConfigureRepository,
    private val botConfig: BotConfig,
    private val dictionary: Dictionary,
    private val easyKeyValueService: EasyKeyValueService
) : CommandExecutor(botConfig), Configurable {
    private val log = getLogger()
    override fun getFunctionId(): FunctionId {
        return FunctionId.ASK_WORLD
    }

    final override fun command(): Command {
        return Command.ASK_WORLD
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val context = dictionary.createContext(update)
        val currentChat = update.toChat()

        val chatEasyKey = currentChat.key()
        val chatUsages = easyKeyValueService.get(AskWorldChatUsages, chatEasyKey)
        if (chatUsages != null && chatUsages > 0L) {
            return {
                log.info("Limit was exceed for chat")
                it.send(update, context.get(Phrase.ASK_WORLD_LIMIT_BY_CHAT), replyToUpdate = true)
            }
        }
        val userEasyKey = update.toUser().key()
        val userUsages = easyKeyValueService.get(AskWorldUserUsages, userEasyKey)
        if (userUsages != null && userUsages > 1) {
            return {
                log.info("Limit was exceed for user")
                it.send(update, context.get(Phrase.ASK_WORLD_LIMIT_BY_USER), replyToUpdate = true)
            }
        }
        val askWorldData = getAskWorldData(update, context)
        if (askWorldData is ValidationError) {
            return askWorldData.invalidQuestionAction
        }

        val successData = askWorldData as Success
        val question = AskWorldQuestion(
            null,
            successData.questionTitle,
            update.toUser(),
            currentChat,
            Instant.now(),
            null
        )
        return { sender ->
            val questionId = coroutineScope { async { askWorldRepository.addQuestion(question) } }
            sender.send(update, context.get(Phrase.DATA_CONFIRM))
            getChatsToSendQuestion(currentChat, successData.isScam)
                .forEach { chatToSend ->
                    runCatching {
                        delay(100)
                        val result = successData.action.invoke(sender, chatToSend, currentChat)
                        markQuestionDelivered(question, questionId, result, chatToSend)
                    }.onFailure { e -> markChatInactive(chatToSend, questionId, e) }
                }
            if (chatUsages == null) {
                easyKeyValueService.put(AskWorldChatUsages, chatEasyKey, 1, untilNextDay())
            } else {
                easyKeyValueService.increment(AskWorldChatUsages, chatEasyKey)
            }
            if (userUsages == null) {
                easyKeyValueService.put(AskWorldUserUsages, userEasyKey, 1, untilNextDay())
            } else {
                easyKeyValueService.increment(AskWorldUserUsages, userEasyKey)
            }
        }
    }

    private fun getAskWorldData(update: Update, context: DictionaryContext): AskWorldQuestionData {
        val replyToMessage = update.message.replyToMessage
        val isReply = update.message.isReply

        if (isReply && replyToMessage.hasPoll()) {
            val poll = replyToMessage.poll
            return Success(
                poll.question,
                false
            ) { sender, chatToSend, currentChat ->
                sender.execute(
                    SendMessage(
                        chatToSend.idString,
                        formatPollMessage(currentChat, chatToSend)
                    ).also { it.enableHtml(true) }
                )
                sender.execute(
                    ForwardMessage(
                        chatToSend.idString,
                        currentChat.idString,
                        replyToMessage.messageId
                    )
                )
            }
        } else {
            val message = if (isReply && replyToMessage.from.id == update.message.from.id) {
                replyToMessage.text
            } else {
                update.message
                    ?.text
                    ?.removePrefix(command().command)
                    ?.removePrefix("@${botConfig.botname}")
                    ?.removePrefix(" ")
            }
                ?.takeIf(String::isNotEmpty) ?: return ValidationError {
                it.send(
                    update,
                    context.get(Phrase.ASK_WORLD_HELP)
                )
            }

            val isScam = containsUrl(message) ||
                isSpam(message) ||
                containsLongWords(message)

            if (message.length > 2000) {
                return ValidationError {
                    it.send(
                        update,
                        context.get(Phrase.ASK_WORLD_QUESTION_TOO_LONG),
                        replyToUpdate = true
                    )
                }
            }
            return Success(message, isScam) { sender, chatToSend, currentChat ->
                sender.execute(
                    SendMessage(
                        chatToSend.idString,
                        formatMessage(currentChat, message, chatToSend)
                    ).also { it.enableHtml(true) }
                )
            }
        }
    }

    private suspend fun markChatInactive(
        chat: Chat,
        questionId: Deferred<Long>,
        e: Throwable
    ) {
        coroutineScope { launch { commonRepository.changeChatActiveStatus(chat, false) } }
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

    private fun formatMessage(currentChat: Chat, question: String, chatToSend: Chat): String {
        val messagePrefix = dictionary.get(Phrase.ASK_WORLD_QUESTION_FROM_CHAT, chatToSend.key())
        val boldChatName = currentChat.name.boldNullable()
        val italicMessage = question.italic()
        return "$messagePrefix $boldChatName: $italicMessage"
    }

    private fun formatPollMessage(currentChat: Chat, chatToSend: Chat): String {
        val messagePrefix = dictionary.get(Phrase.ASK_WORLD_QUESTION_FROM_CHAT, chatToSend.key())
        val boldChatName = currentChat.name.boldNullable()
        return "$messagePrefix $boldChatName:"
    }

    private fun isEnabledInChat(it: Chat) = configureRepository.isEnabled(getFunctionId(), it)

    private fun getChatsToSendQuestion(currentChat: Chat, isScam: Boolean): List<Chat> {
        if (isScam) {
            log.info("Some scam message was found and it won't be sent")
            return emptyList()
        }

        val chatsWithFeatureEnabled = commonRepository.getChats()
            .filterNot { it == currentChat }
            .filter(this@AskWorldInitialExecutor::isEnabledInChat)
            .shuffled()
        log.info("Number of chats with feature enabled: ${chatsWithFeatureEnabled.size}")
        val acceptAllChats = chatsWithFeatureEnabled
            .filter { chat -> getDensity(chat) == AskWorldDensityValue.ALL }
        val acceptLessChats = chatsWithFeatureEnabled
            .filter { chat -> getDensity(chat) == AskWorldDensityValue.LESS }
        log.info("Number of chats with ${AskWorldDensityValue.ALL} density: ${acceptAllChats.size}")
        log.info("Number of chats with ${AskWorldDensityValue.LESS} density: ${acceptLessChats.size}")

        return acceptAllChats + acceptLessChats.take(acceptLessChats.size / 4)
    }

    private fun containsUrl(message: String): Boolean {
        return message.contains("http", ignoreCase = true) ||
            message.contains("www", ignoreCase = true) ||
            message.contains("jpg", ignoreCase = true) ||
            message.contains("png", ignoreCase = true) ||
            message.contains("jpeg", ignoreCase = true) ||
            message.contains("bmp", ignoreCase = true) ||
            message.contains("gif", ignoreCase = true) ||
            message.contains("_bot", ignoreCase = true) ||
            message.contains("@", ignoreCase = true) ||
            message.contains("Bot", ignoreCase = true)
    }

    private fun isSpam(message: String): Boolean {
        return askWorldRepository
            .findQuestionByText(
                message,
                date = Instant.now().minus(30, ChronoUnit.DAYS)
            ).isNotEmpty()
    }

    private fun containsLongWords(message: String): Boolean {
        return message.split(" ").any { it.length > 30 }
    }

    private fun getDensity(chat: Chat): AskWorldDensityValue {
        val settingValue = easyKeyValueService.get(
            AskWorldDensity,
            chat.key()
        ) ?: return AskWorldDensityValue.LESS
        return AskWorldDensityValue
            .values()
            .find { value -> value.text == settingValue } ?: AskWorldDensityValue.LESS
    }
}
