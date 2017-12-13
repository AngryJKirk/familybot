package space.yaroslav.familybot

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import space.yaroslav.familybot.route.Router


@Component
class FamilyBot(val config: BotConfig, val router: Router) : TelegramLongPollingBot() {

    override fun getBotToken(): String {
        return config.token!!
    }

    override fun onUpdateReceived(update: Update?) {
        execute(router.processUpdate(update!!))
    }

    override fun getBotUsername(): String {
        return config.botname!!
    }


}
