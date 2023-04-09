package dev.storozhenko.familybot.feature.backend

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.OnlyBotOwnerExecutor
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.repos.UserRepository
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.getLogger
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ForwardMessage

@Component
class PatchNoteExecutor(
    private val commonRepository: UserRepository
) : OnlyBotOwnerExecutor() {

    private val patchNotePrefix = "patch_note"
    private val log = getLogger()

    override suspend fun executeInternal(context: ExecutorContext) {
        if (context.message.isReply.not()) {
            context.sender.send(context, "No reply message found, master")
            return
        }

        val chats = commonRepository.getChats()
        log.info("Sending in {} chats", chats.size)
        chats.forEach { tryToSendMessage(it, context) }
    }

    override fun getMessagePrefix() = patchNotePrefix

    private fun markChatAsInactive(chat: Chat) {
        commonRepository.changeChatActiveStatus(chat, false)
    }

    private suspend fun tryToSendMessage(
        chat: Chat,
        context: ExecutorContext
    ) {
        coroutineScope {
            launch {
                delay(500)
                runCatching {
                    context.sender.execute(
                        ForwardMessage(
                            chat.idString,
                            context.user.id.toString(),
                            context.message.replyToMessage.messageId
                        )
                    )
                    log.info("Sent patchnote to chatId={}", chat.idString)
                }.onFailure { throwable ->
                    log.warn("Can not send message by patchnote executor", throwable)
                    markChatAsInactive(chat)
                }
            }
        }
    }
}
