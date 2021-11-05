package space.yaroslav.familybot.executors.pm

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ForwardMessage
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.telegram.BotConfig

@Component
class PatchNoteExecutor(
    private val commonRepository: CommonRepository,
    botConfig: BotConfig
) : OnlyBotOwnerExecutor(botConfig) {

    private val patchNotePrefix = "patch_note"
    private val log = getLogger()

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        if (context.message.isReply.not()) {
            return { sender -> sender.send(context, "No reply message found, master") }
        }
        return { sender ->
            val chats = commonRepository.getChats()
            log.info("Sending in {} chats", chats.size)
            chats.forEach { tryToSendMessage(sender, it, context) }
        }
    }

    override fun getMessagePrefix() = patchNotePrefix

    private fun markChatAsInactive(chat: Chat) {
        commonRepository.changeChatActiveStatus(chat, false)
    }

    private suspend fun tryToSendMessage(
        sender: AbsSender,
        chat: Chat,
        context: ExecutorContext
    ) {
        coroutineScope {
            launch {
                delay(500)
                runCatching {
                    sender.execute(
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
