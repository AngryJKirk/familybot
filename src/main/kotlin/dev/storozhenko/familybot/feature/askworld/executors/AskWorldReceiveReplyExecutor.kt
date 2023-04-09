package dev.storozhenko.familybot.feature.askworld.executors

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.boldNullable
import dev.storozhenko.familybot.common.extensions.italic
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.Configurable
import dev.storozhenko.familybot.core.executors.Executor
import dev.storozhenko.familybot.core.keyvalue.models.ChatEasyKey
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.MessageContentType
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority
import dev.storozhenko.familybot.core.telegram.FamilyBot
import dev.storozhenko.familybot.feature.askworld.models.AskWorldReply
import dev.storozhenko.familybot.feature.askworld.repos.AskWorldRepository
import dev.storozhenko.familybot.feature.settings.models.FunctionId
import dev.storozhenko.familybot.feature.talking.services.Dictionary
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation
import org.telegram.telegrambots.meta.api.methods.send.SendAudio
import org.telegram.telegrambots.meta.api.methods.send.SendContact
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendLocation
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.send.SendSticker
import org.telegram.telegrambots.meta.api.methods.send.SendVideo
import org.telegram.telegrambots.meta.api.methods.send.SendVideoNote
import org.telegram.telegrambots.meta.api.methods.send.SendVoice
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.bots.AbsSender
import java.time.Instant

@Component
class AskWorldReceiveReplyExecutor(
    private val askWorldRepository: AskWorldRepository,
    private val botConfig: BotConfig,
    private val dictionary: Dictionary
) : Executor, Configurable {
    private val log = LoggerFactory.getLogger(AskWorldReceiveReplyExecutor::class.java)
    override fun getFunctionId(context: ExecutorContext): FunctionId {
        return FunctionId.ASK_WORLD
    }

    override fun canExecute(context: ExecutorContext): Boolean {
        val message = context.message
        if (message.isReply.not()) {
            return false
        }

        val chatId = message.chatId
        val messageId = message.replyToMessage.messageId
        if (message.replyToMessage.hasPoll()) {
            return askWorldRepository.findQuestionByMessageId(messageId + chatId, chatId) != null
        }

        val text = message.replyToMessage
            .takeIf { it.from.isBot && it.from.userName == botConfig.botName }
            ?.text
            ?: return false

        val allPrefixes = dictionary.getAll(Phrase.ASK_WORLD_QUESTION_FROM_CHAT)

        return allPrefixes.map { "$it " }.any { text.startsWith(it) }
    }

    override fun priority(context: ExecutorContext): Priority {
        return Priority.LOW
    }

    override suspend fun execute(context: ExecutorContext) {
        val message = context.message
        val reply = message.text ?: "MEDIA: $message"
        val chat = context.chat
        val user = context.user
        val chatId = chat.id
        val messageId = message.replyToMessage.messageId
        val question =
            askWorldRepository.findQuestionByMessageId(messageId + chatId, chatId) ?: return
        if (askWorldRepository.isReplied(question, chat, user)) {
            context.sender.execute(
                SendMessage(
                    chat.idString,
                    context.phrase(Phrase.ASK_WORLD_ANSWER_COULD_BE_ONLY_ONE)
                ).apply {
                    replyToMessageId = message.messageId
                }
            )
        }
        val contentType = detectContentType(message)
        val askWorldReply = AskWorldReply(
            null,
            question.id
                ?: throw FamilyBot.InternalException("Question id is missing, seems like internal logic error"),
            reply,
            user,
            chat,
            Instant.now()
        )

        runCatching {
            coroutineScope { launch { askWorldRepository.addReply(askWorldReply) } }
            val questionTitle = question.message.takeIf { it.length < 100 } ?: (question.message.take(100) + "...")
            val chatIdToReply = question.chat.idString

            val answerTitle = dictionary.get(Phrase.ASK_WORLD_REPLY_FROM_CHAT, ChatEasyKey(question.chat.id))
            if (contentType == MessageContentType.TEXT) {
                sendAnswerWithQuestion(
                    context.sender,
                    chatIdToReply,
                    answerTitle,
                    context,
                    questionTitle,
                    reply
                )
            } else {
                sendOnlyQuestion(
                    context.sender,
                    chatIdToReply,
                    answerTitle,
                    context,
                    questionTitle
                )
                dispatchMedia(context.sender, contentType, chatIdToReply, message)
            }
            context.sender.send(context, "Принято и отправлено")
        }.onFailure { e ->
            context.sender.send(context, "Принято")
            log.info("Could not send reply instantly", e)
        }
    }

    private fun dispatchMedia(
        sender: AbsSender,
        contentType: MessageContentType,
        chatIdToReply: String,
        message: Message
    ) {
        when (contentType) {
            MessageContentType.PHOTO ->
                sender.execute(sendPhoto(chatIdToReply, message))

            MessageContentType.AUDIO ->
                sender.execute(sendAudio(chatIdToReply, message))

            MessageContentType.ANIMATION -> sender.execute(sendAnimation(chatIdToReply, message))

            MessageContentType.DOCUMENT -> sender.execute(sendDocument(chatIdToReply, message))

            MessageContentType.VOICE ->
                sender.execute(sendVoice(chatIdToReply, message))

            MessageContentType.VIDEO_NOTE ->
                sender.execute(sendVideoNote(chatIdToReply, message))

            MessageContentType.LOCATION -> sender.execute(sendLocation(chatIdToReply, message))

            MessageContentType.STICKER -> sender.execute(sendSticker(chatIdToReply, message))

            MessageContentType.CONTACT -> sender.execute(sendContact(chatIdToReply, message))

            MessageContentType.VIDEO -> sender.execute(sendVideo(chatIdToReply, message))
            else -> log.warn("Something went wrong with content type detection logic")
        }
    }

    private fun sendVideo(
        chatIdToReply: String,
        message: Message
    ): SendVideo {
        return SendVideo(
            chatIdToReply,
            InputFile(message.video.fileId)
        ).apply {
            if (message.hasText()) {
                caption = message.text
            }
        }
    }

    private fun sendContact(
        chatIdToReply: String,
        message: Message
    ): SendContact {
        return SendContact(chatIdToReply, message.contact.phoneNumber, message.contact.firstName).apply {
            lastName = message.contact.lastName
        }
    }

    private fun sendSticker(
        chatIdToReply: String,
        message: Message
    ): SendSticker {
        return SendSticker(chatIdToReply, InputFile(message.sticker.fileId))
    }

    private fun sendLocation(
        chatIdToReply: String,
        message: Message
    ): SendLocation {
        return SendLocation(
            chatIdToReply,
            message.location.latitude,
            message.location.longitude
        )
    }

    private fun sendVideoNote(
        chatIdToReply: String,
        message: Message
    ): SendVideoNote {
        return SendVideoNote(chatIdToReply, InputFile(message.videoNote.fileId))
    }

    private fun sendVoice(
        chatIdToReply: String,
        message: Message
    ): SendVoice {
        return SendVoice(chatIdToReply, InputFile(message.voice.fileId))
    }

    private fun sendDocument(
        chatIdToReply: String,
        message: Message
    ): SendDocument {
        return SendDocument(chatIdToReply, InputFile(message.document.fileId)).apply {
            if (message.hasText()) {
                caption = message.text
            }
        }
    }

    private fun sendAnimation(
        chatIdToReply: String,
        message: Message
    ): SendAnimation {
        return SendAnimation(chatIdToReply, InputFile(message.animation.fileId))
    }

    private fun sendAudio(
        chatIdToReply: String,
        message: Message
    ): SendAudio {
        return SendAudio(
            chatIdToReply,
            InputFile(message.audio.fileId)
        ).apply {
            if (message.hasText()) {
                caption = message.text
            }
        }
    }

    private fun sendPhoto(
        chatIdToReply: String,
        message: Message
    ): SendPhoto {
        return SendPhoto(chatIdToReply, InputFile(message.photo.first().fileId))
            .apply {
                if (message.hasText()) {
                    caption = message.text
                }
            }
    }

    private fun sendOnlyQuestion(
        it: AbsSender,
        chatIdToReply: String,
        answerTitle: String,
        context: ExecutorContext,
        questionTitle: String
    ) {
        it.execute(
            SendMessage(
                chatIdToReply,
                "$answerTitle ${context.chat.name.boldNullable()} " +
                        "от ${context.user.getGeneralName()} на вопрос \"$questionTitle\":"
            ).apply {
                enableHtml(true)
            }
        )
    }

    private fun sendAnswerWithQuestion(
        it: AbsSender,
        chatIdToReply: String,
        answerTitle: String,
        context: ExecutorContext,
        questionTitle: String,
        reply: String
    ) {
        it.execute(
            SendMessage(
                chatIdToReply,
                "$answerTitle ${context.chat.name.boldNullable()} " +
                        "от ${context.user.getGeneralName()} на вопрос \"$questionTitle\": ${reply.italic()}"
            ).apply {
                enableHtml(true)
            }
        )
    }

    private fun detectContentType(message: Message): MessageContentType {
        return when {
            message.hasLocation() -> MessageContentType.LOCATION
            message.hasAnimation() -> MessageContentType.ANIMATION
            message.hasAudio() -> MessageContentType.AUDIO
            message.hasContact() -> MessageContentType.CONTACT
            message.hasDocument() -> MessageContentType.DOCUMENT
            message.hasPhoto() -> MessageContentType.PHOTO
            message.hasSticker() -> MessageContentType.STICKER
            message.hasVideoNote() -> MessageContentType.VIDEO_NOTE
            message.hasVideo() -> MessageContentType.VIDEO
            message.hasVoice() -> MessageContentType.VOICE
            else -> MessageContentType.TEXT
        }
    }
}
