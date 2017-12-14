package space.yaroslav.familybot.route

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.random
import space.yaroslav.familybot.repos.KeywordRepository

@Component
class KeyWordExecutor(val keyset: KeywordRepository) : Executor {
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