package space.yaroslav.familybot.telegram

import kotlinx.coroutines.experimental.launch
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.route.Router


@Component
class FamilyBot(val config: BotConfig, val router: Router) : TelegramLongPollingBot() {


    override fun getBotToken(): String {
        return config.token!!
    }

    override fun onUpdateReceived(update: Update?) {
        val toUser = update!!.toUser()
        MDC.put("chat", "${toUser.chat.name}:${toUser.chat.id}")
        MDC.put("user", "${toUser.name}:${toUser.id}")
        router.processUpdate(update).invoke(this).also { launch {  MDC.clear() } }
    }

    override fun getBotUsername(): String {
        return config.botname!!
    }


}
