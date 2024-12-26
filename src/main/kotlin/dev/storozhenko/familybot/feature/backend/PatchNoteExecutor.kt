package dev.storozhenko.familybot.feature.backend


import dev.storozhenko.familybot.core.executors.OnlyBotOwnerExecutor
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.repos.UserRepository
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.telegram.telegrambots.meta.api.methods.ForwardMessage

abstract class PatchNoteExecutor(
    private val commonRepository: UserRepository,
) : OnlyBotOwnerExecutor() {

    private val log = KotlinLogging.logger { }

    override suspend fun executeInternal(context: ExecutorContext) {
        if (context.message.isReply.not()) {
            context.send("No reply message found, master")
            return
        }

        val chats = getChats()
        log.info { "Sending in ${chats.size} chats" }
        val results = chats.map { tryToSendMessage(it, context) }
        val message = results.awaitAll()
            .groupBy { it }
            .let { "${it[true]?.size ?: 0} sent, ${it[false]?.size ?: 0} failed" }
        context.send(message)
    }

    abstract fun getChats(): List<Chat>

    private fun markChatAsInactive(chat: Chat) {
        commonRepository.changeChatActiveStatus(chat, false)
    }

    private suspend fun tryToSendMessage(
        chat: Chat,
        context: ExecutorContext,
    ): Deferred<Boolean> {
        return coroutineScope {
            async {
                delay(500)
                runCatching {
                    context.client.execute(
                        ForwardMessage(
                            chat.idString,
                            context.user.id.toString(),
                            context.message.replyToMessage.messageId,
                        ),
                    )
                    log.info { "Sent patchnote to chatId=${chat.idString}" }
                    true
                }.onFailure { throwable ->
                    log.warn(throwable) { "Can not send message by patchnote executor" }
                    markChatAsInactive(chat)
                }.getOrDefault(false)
            }
        }
    }
}
