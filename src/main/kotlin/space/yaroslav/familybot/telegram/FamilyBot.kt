package space.yaroslav.familybot.telegram

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import space.yaroslav.familybot.route.Router


@Component
class FamilyBot(val config: BotConfig, val router: Router) : TelegramLongPollingBot() {



    override fun getBotToken(): String {
        return config.token!!
    }

    override fun onUpdateReceived(update: Update?) = router.processUpdate(update!!).invoke(this)

    override fun getBotUsername(): String {
        return config.botname!!
    }



}
