package space.yaroslav.familybot.route

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.toChat
import space.yaroslav.familybot.repos.CommonRepository

@Component
class PidorStatsExecutor(val repository: CommonRepository) : Executor {
    override fun execute(update: Update):  (AbsSender) -> Unit {
        val pidorsByChat = repository.getPidorsByChat(update.message.chat.toChat())
        val groupBy = pidorsByChat
                .groupBy { it.user }
                .map { it.key to it.value.size }
                .sortedBy { it.second }
                .map { "${it.first.getGeneralName()} был пидором ${it.second} раз" }
        val title = "<b>Список пидоров на сегодня</b>:\n"
        return { it.execute(SendMessage(update.message.chatId, title + groupBy.joinToString("\n")).enableHtml(true)) }

    }


    override fun canExecute(message: Message): Boolean {
        return message.text?.contains("/stats") ?: false
    }
}