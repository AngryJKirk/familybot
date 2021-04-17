package space.yaroslav.familybot.executors.eventbased

import kotlinx.coroutines.async
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
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.AskWorldReply
import space.yaroslav.familybot.common.utils.boldNullable
import space.yaroslav.familybot.common.utils.italic
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.FunctionId
import space.yaroslav.familybot.models.MessageContentType
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.models.Priority
import space.yaroslav.familybot.repos.AskWorldRepository
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.telegram.BotConfig
import space.yaroslav.familybot.telegram.FamilyBot
import java.time.Instant

@Component
class AskWorldReceiveReplyExecutor(
    private val askWorldRepository: AskWorldRepository,
    private val botConfig: BotConfig,
    private val dictionary: Dictionary
) : Executor, Configurable {
    private val log = LoggerFactory.getLogger(AskWorldReceiveReplyExecutor::class.java)
    override fun getFunctionId(): FunctionId {
        return FunctionId.ASK_WORLD
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val message = update.message
        val reply = message.text ?: "MEDIA: $message"
        val chat = update.toChat()
        val user = update.toUser()
        val question =
            askWorldRepository.findQuestionByMessageId(message.replyToMessage.messageId + chat.id, chat)

        if (askWorldRepository.isReplied(question, chat, user)) {
            return {
                it.execute(
                    SendMessage(
                        chat.idString,
                        "Отвечать можно только раз"
                    ).apply {
                        replyToMessageId = message.messageId
                    }
                )
            }
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

        return {
            runCatching {
                val context = dictionary.createContext(update)
                val id = coroutineScope { async { askWorldRepository.addReply(askWorldReply) } }
                val questionTitle = question.message.takeIf { it.length < 100 } ?: question.message.take(100) + "..."
                val chatIdToReply = question.chat.idString

                if (contentType == MessageContentType.TEXT) {
                    it.execute(
                        SendMessage(
                            chatIdToReply,
                            "${context.get(Phrase.ASK_WORLD_REPLY_FROM_CHAT)} ${update.toChat().name.boldNullable()} " +
                                "от ${user.getGeneralName()} на вопрос \"$questionTitle\": ${reply.italic()}"
                        ).apply {
                            enableHtml(true)
                        }
                    )
                } else {
                    it.execute(
                        SendMessage(
                            chatIdToReply,
                            "${context.get(Phrase.ASK_WORLD_REPLY_FROM_CHAT)} ${update.toChat().name.boldNullable()} " +
                                "от ${user.getGeneralName()} на вопрос \"$questionTitle\":"
                        ).apply {
                            enableHtml(true)
                        }
                    )
                    when (contentType) {
                        MessageContentType.PHOTO ->
                            it.execute(SendPhoto(chatIdToReply, InputFile(message.photo.first().fileId)))

                        MessageContentType.AUDIO ->
                            it.execute(
                                SendAudio(
                                    chatIdToReply,
                                    InputFile(message.audio.fileId)
                                )
                            )

                        MessageContentType.ANIMATION -> it.execute(
                            SendAnimation(chatIdToReply, InputFile(message.animation.fileId))
                        )

                        MessageContentType.DOCUMENT -> it.execute(
                            SendDocument(chatIdToReply, InputFile(message.document.fileId))
                        )

                        MessageContentType.VOICE ->
                            it.execute(SendVoice(chatIdToReply, InputFile(message.voice.fileId)))

                        MessageContentType.VIDEO_NOTE ->
                            it.execute(SendVideoNote(chatIdToReply, InputFile(message.videoNote.fileId)))

                        MessageContentType.LOCATION -> it.execute(
                            SendLocation(
                                chatIdToReply,
                                message.location.latitude,
                                message.location.longitude
                            )
                        )

                        MessageContentType.STICKER -> it.execute(
                            SendSticker(chatIdToReply, InputFile(message.sticker.fileId))
                        )

                        MessageContentType.CONTACT -> it.execute(
                            SendContact(chatIdToReply, message.contact.phoneNumber, message.contact.firstName).apply {
                                lastName = message.contact.lastName
                            }
                        )

                        MessageContentType.VIDEO -> it.execute(
                            SendVideo(
                                chatIdToReply,
                                InputFile(message.video.fileId)
                            )
                        )
                        else -> log.warn("Something went wrong with content type detection logic")
                    }
                }
                coroutineScope { launch { askWorldRepository.addReplyDeliver(askWorldReply.copy(id = id.await())) } }
                it.send(update, "Принято и отправлено")
            }.onFailure { e ->
                it.send(update, "Принято")
                log.info("Could not send reply instantly", e)
            }
        }
    }

    override fun canExecute(message: Message): Boolean {
        val text = message.replyToMessage
            ?.takeIf { it.from.isBot && it.from.userName == botConfig.botname }
            ?.text ?: return false

        val allPrefixes = dictionary.getAll(Phrase.ASK_WORLD_QUESTION_FROM_CHAT)

        return allPrefixes.map { "$it " }.any { text.startsWith(it) }
    }

    override fun priority(update: Update): Priority {
        return Priority.LOW
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
