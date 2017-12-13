package space.yaroslav.familybot.route

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.common.toChat

@Component
class PidorStatsExecutor(val repository: CommonRepository) : Executor {
    override fun execute(update: Update): SendMessage? {
        val pidorsByChat = repository.getPidorsByChat(update.message.chat.toChat())
        val groupBy = pidorsByChat
                .groupBy { it.user }
                .map { it.key to it.value.size }
                .sortedBy { it.second }
                .map { "${it.first.getGeneralName()} был пидором ${it.second} раз" }
        val title = "<b>Список пидоров на сегодня</b>:\n"
        return SendMessage(update.message.chatId, title + groupBy.joinToString("\n")).enableHtml(true)

    }


    override fun canExecute(update: Update): Boolean {
        return update.message.text.contains("/stats")
    }
}