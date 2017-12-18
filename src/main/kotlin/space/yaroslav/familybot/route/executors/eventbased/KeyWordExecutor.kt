package space.yaroslav.familybot.route.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.random
import space.yaroslav.familybot.repos.ifaces.KeywordRepository
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.Priority

@Component
class KeyWordExecutor(val keyset: KeywordRepository) : Executor {
    override fun priority(): Priority {
        return Priority.MEDIUM
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val phrasesByKeyword = keyset.getPhrasesByKeyword(findKeyword(update.message.text)!!)
        return { it.execute(SendMessage(update.message.chatId, phrasesByKeyword.random()))}
    }

    override fun canExecute(message: Message): Boolean {
        return findKeyword(message.text) != null
    }

    private fun findKeyword(phrase: String?): String?{

        return phrase?.split(" ")?.find { keyset.getKeywords().contains(it) }
    }
}