package space.yaroslav.familybot.executors.pm

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.models.Priority
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.telegram.BotConfig

@Component
class PatchNoteExecutor(private val botConfig: BotConfig, private val commonRepository: CommonRepository) :
    PrivateMessageExecutor {
    private val patchnotePrefix = "PATCHNOTE1488"

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        return { sender ->
            commonRepository
                .getChats()
                .forEach { tryToSendMessage(sender, it, update) }
        }
    }

    override fun canExecute(message: Message): Boolean {
        return botConfig.developer == message.from.userName && message.text.startsWith(patchnotePrefix)
    }

    override fun priority(update: Update): Priority {
        return Priority.HIGH
    }

    private fun markChatAsInactive(chat: Chat) {
        commonRepository.changeChatActiveStatus(chat, false)
    }

    private fun tryToSendMessage(sender: AbsSender, chat: Chat, update: Update) {
        GlobalScope.launch {
            runCatching {
                sender.execute(SendMessage(chat.idString, update.message.text.removePrefix(patchnotePrefix)))
            }.onFailure { markChatAsInactive(chat) }
        }
    }
}
