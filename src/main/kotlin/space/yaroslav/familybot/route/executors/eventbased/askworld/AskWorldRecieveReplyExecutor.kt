package space.yaroslav.familybot.route.executors.eventbased.askworld

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation
import org.telegram.telegrambots.meta.api.methods.send.SendAudio
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.send.SendSticker
import org.telegram.telegrambots.meta.api.methods.send.SendVideo
import org.telegram.telegrambots.meta.api.methods.send.SendVideoNote
import org.telegram.telegrambots.meta.api.methods.send.SendVoice
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.AskWorldReply
import space.yaroslav.familybot.common.utils.boldNullable
import space.yaroslav.familybot.common.utils.italic
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.AskWorldRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.route.models.MessageContentType
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.models.Priority
import space.yaroslav.familybot.route.services.dictionary.Dictionary
import space.yaroslav.familybot.telegram.BotConfig
import space.yaroslav.familybot.telegram.FamilyBot
import java.time.Instant

@Component
class AskWorldRecieveReplyExecutor(
    val askWorldRepository: AskWorldRepository,
    val botConfig: BotConfig,
    val dictionary: Dictionary
) : Executor, Configurable {
    private val log = LoggerFactory.getLogger(AskWorldRecieveReplyExecutor::class.java)
    override fun getFunctionId(): FunctionId {
        return FunctionId.ASK_WORLD
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val reply = update.message.text ?: "" // todo
        val chat = update.toChat()
        val user = update.toUser()
        val question =
            askWorldRepository.findQuestionByMessageId(update.message.replyToMessage.messageId + chat.id, chat)

        if (askWorldRepository.isReplied(question, chat, user)) {
            return {
                it.execute(
                    SendMessage(
                        chat.id,
                        "Отвечать можно только раз"
                    ).setReplyToMessageId(update.message.messageId)
                )
            }
        }
        val contentType = detectContentType(update.message)
        val askWorldReply = AskWorldReply(
            null,
            question.id ?: throw FamilyBot.InternalException("Question id is missing, seems like internal logic error"),
            reply,
            user,
            chat,
            Instant.now(),
            contentType
        )

        return {
            runCatching {
                val id = GlobalScope.async { askWorldRepository.addReply(askWorldReply) }
                val message = question.message.takeIf { it.length < 100 } ?: question.message.take(100) + "..."
                val chatIdToReply = question.chat.id

                if (contentType == MessageContentType.TEXT) {
                    it.execute(
                        SendMessage(
                            chatIdToReply,
                            "${dictionary.get(Phrase.ASK_WORLD_REPLY_FROM_CHAT)} ${update.toChat().name.boldNullable()} " +
                                "от ${user.getGeneralName()} на вопрос \"$message\": ${reply.italic()}"
                        ).enableHtml(true)
                    )
                } else {
                    it.execute(
                        SendMessage(
                            chatIdToReply,
                        "${dictionary.get(Phrase.ASK_WORLD_REPLY_FROM_CHAT)} ${update.toChat().name.boldNullable()} " +
                            "от ${user.getGeneralName()} на вопрос \"$message\":"
                        ).enableHtml(true)
                    )
                    when (contentType) {
                        MessageContentType.PHOTO -> it.execute(SendPhoto().setChatId(chatIdToReply).setPhoto(update.message.photo.first().fileId))
                        MessageContentType.AUDIO -> it.execute(SendAudio().setChatId(chatIdToReply).setAudio(update.message.audio.fileId))
                        MessageContentType.ANIMATION -> it.execute(
                            SendAnimation().setChatId(chatIdToReply).setAnimation(
                                update.message.animation.fileId
                            )
                        )
                        MessageContentType.DOCUMENT -> it.execute(
                            SendDocument().setChatId(chatIdToReply).setDocument(
                                update.message.document.fileId
                            )
                        )
                        MessageContentType.VOICE -> it.execute(SendVoice().setChatId(chatIdToReply).setVoice(update.message.voice.fileId))
                        MessageContentType.VIDEO_NOTE -> it.execute(
                            SendVideoNote().setChatId(chatIdToReply).setVideoNote(
                                update.message.videoNote.fileId
                            )
                        )
                        MessageContentType.LOCATION -> TODO()
                        MessageContentType.STICKER -> it.execute(
                            SendSticker().setChatId(chatIdToReply).setSticker(
                                update.message.sticker.fileId
                            )
                        )
                        MessageContentType.CONTACT -> TODO()
                        MessageContentType.VIDEO -> it.execute(SendVideo().setChatId(chatIdToReply).setVideo(update.message.video.fileId))
                        else -> TODO()
                    }
                }
                GlobalScope.launch { askWorldRepository.addReplyDeliver(askWorldReply.copy(id = id.await())) }
                it.send(update, "Принято и отправлено")
            }.onFailure { e ->
                it.send(update, "Принято")
                log.info("Could not send reply instantly", e)
            }
        }
    }

    override fun canExecute(message: Message): Boolean {
        val text = message.replyToMessage
            ?.takeIf { it.from.bot && it.from.userName == botConfig.botname }
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
