package dev.storozhenko.familybot.feature.askworld.executors

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.boldNullable
import dev.storozhenko.familybot.common.extensions.italic
import dev.storozhenko.familybot.common.extensions.key
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.common.extensions.untilNextDay
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.executors.Configurable
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.repos.UserRepository
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.askworld.models.AskWorldQuestion
import dev.storozhenko.familybot.feature.askworld.models.AskWorldQuestionData
import dev.storozhenko.familybot.feature.askworld.models.Success
import dev.storozhenko.familybot.feature.askworld.models.ValidationError
import dev.storozhenko.familybot.feature.askworld.repos.AskWorldRepository
import dev.storozhenko.familybot.feature.settings.models.AskWorldChatUsages
import dev.storozhenko.familybot.feature.settings.models.AskWorldDensity
import dev.storozhenko.familybot.feature.settings.models.AskWorldUserUsages
import dev.storozhenko.familybot.feature.settings.models.FunctionId
import dev.storozhenko.familybot.feature.settings.processors.AskWorldDensityValue
import dev.storozhenko.familybot.feature.settings.repos.FunctionsConfigureRepository
import dev.storozhenko.familybot.feature.talking.services.Dictionary
import dev.storozhenko.familybot.getLogger
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ForwardMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class AskWorldInitialExecutor(
    private val askWorldRepository: AskWorldRepository,
    private val commonRepository: UserRepository,
    private val configureRepository: FunctionsConfigureRepository,
    private val botConfig: BotConfig,
    private val dictionary: Dictionary,
    private val easyKeyValueService: EasyKeyValueService
) : CommandExecutor(), Configurable {
    private val log = getLogger()
    override fun getFunctionId(context: ExecutorContext): FunctionId {
        return FunctionId.ASK_WORLD
    }

    override fun command(): Command {
        return Command.ASK_WORLD
    }

    override suspend fun execute(context: ExecutorContext) {
        val currentChat = context.chat

        val chatKey = context.chatKey
        val userEasyKey = context.userKey
        val chatUsages = easyKeyValueService.get(AskWorldChatUsages, chatKey)
        val userUsages = easyKeyValueService.get(AskWorldUserUsages, userEasyKey)
        val isNotFromDeveloper = !context.isFromDeveloper
        if (isNotFromDeveloper) {
            if (chatUsages != null && chatUsages > 0L) {
                log.info("Limit was exceed for chat")
                context.sender.send(context, context.phrase(Phrase.ASK_WORLD_LIMIT_BY_CHAT), replyToUpdate = true)
                return
            }

            if (userUsages != null && userUsages > 1) {
                log.info("Limit was exceed for user")
                context.sender.send(context, context.phrase(Phrase.ASK_WORLD_LIMIT_BY_USER), replyToUpdate = true)
                return
            }
        }

        val askWorldData = getAskWorldData(context)
        if (askWorldData is ValidationError) {
            askWorldData.invalidQuestionAction.invoke(context.sender)
            return
        }

        val successData = askWorldData as Success
        val question = AskWorldQuestion(
            null,
            successData.questionTitle,
            context.user,
            currentChat,
            Instant.now(),
            null
        )

        val questionId = coroutineScope { async { askWorldRepository.addQuestion(question) } }
        context.sender.send(context, context.phrase(Phrase.DATA_CONFIRM))
        if (!successData.isScam) {
            getChatsToSendQuestion(context)
                .forEach { chatToSend ->
                    runCatching {
                        delay(100)
                        val result = successData.action.invoke(context.sender, chatToSend, currentChat)
                        markQuestionDelivered(question, questionId, result, chatToSend)
                    }.onFailure { e -> markChatInactive(chatToSend, questionId, e) }
                }
        } else {
            log.info("Some scam message was found and it won't be sent")
        }
        if (isNotFromDeveloper) {
            if (chatUsages == null) {
                easyKeyValueService.put(AskWorldChatUsages, chatKey, 1, untilNextDay())
            } else {
                easyKeyValueService.increment(AskWorldChatUsages, chatKey)
            }
            if (userUsages == null) {
                easyKeyValueService.put(AskWorldUserUsages, userEasyKey, 1, untilNextDay())
            } else {
                easyKeyValueService.increment(AskWorldUserUsages, userEasyKey)
            }
        }
    }

    private fun getAskWorldData(context: ExecutorContext): AskWorldQuestionData {
        val replyToMessage = context.message.replyToMessage
        val isReply = context.message.isReply

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
            val message = if (isReply && replyToMessage.from.id == context.message.from.id) {
                replyToMessage.text
            } else {
                context.message
                    .text
                    ?.removePrefix(command().command)
                    ?.removePrefix("@${botConfig.botName}")
                    ?.removePrefix(" ")
            }
                ?.takeIf(String::isNotEmpty) ?: return ValidationError {
                context.sender.send(
                    context,
                    context.phrase(Phrase.ASK_WORLD_HELP)
                )
            }

            val isScam =
                shouldBeCensored(message) ||
                        shouldBeCensored(context.chat.name ?: "") ||
                        isSpam(message) ||
                        containsLongWords(message)

            if (message.length > 2000) {
                return ValidationError {
                    context.sender.send(
                        context,
                        context.phrase(Phrase.ASK_WORLD_QUESTION_TOO_LONG),
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

    private fun getChatsToSendQuestion(
        context: ExecutorContext
    ): List<Chat> {
        val functionId = getFunctionId(context)
        val chatsWithFeatureEnabled = commonRepository.getChats()
            .filterNot { chat -> chat == context.chat }
            .filter { chat -> configureRepository.isEnabled(functionId, chat) }
            .shuffled()
        log.info("Number of chats with feature enabled: ${chatsWithFeatureEnabled.size}")

        if (context.isFromDeveloper) {
            return chatsWithFeatureEnabled
        }

        val acceptAllChats = chatsWithFeatureEnabled
            .filter { chat -> getDensity(chat) == AskWorldDensityValue.ALL }
        val acceptLessChats = chatsWithFeatureEnabled
            .filter { chat -> getDensity(chat) == AskWorldDensityValue.LESS }
        log.info("Number of chats with ${AskWorldDensityValue.ALL} density: ${acceptAllChats.size}")
        log.info("Number of chats with ${AskWorldDensityValue.LESS} density: ${acceptLessChats.size}")

        return acceptAllChats + acceptLessChats.take(acceptLessChats.size / 4)
    }

    private fun shouldBeCensored(message: String): Boolean {
        return message.contains("http", ignoreCase = true) ||
                message.contains("www", ignoreCase = true) ||
                message.contains("jpg", ignoreCase = true) ||
                message.contains("png", ignoreCase = true) ||
                message.contains("jpeg", ignoreCase = true) ||
                message.contains("bmp", ignoreCase = true) ||
                message.contains("gif", ignoreCase = true) ||
                message.contains("_bot", ignoreCase = true) ||
                message.contains("t.me", ignoreCase = true) ||
                message.contains("Bot", ignoreCase = false) ||
                message.contains("@")
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
