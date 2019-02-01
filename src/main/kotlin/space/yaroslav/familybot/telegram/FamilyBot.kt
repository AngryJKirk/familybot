package space.yaroslav.familybot.telegram

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.exceptions.TelegramApiRequestException
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.route.Router

@Component
class FamilyBot(val config: BotConfig, val router: Router) : TelegramLongPollingBot() {

    private final val log = LoggerFactory.getLogger(FamilyBot::class.java)

    override fun getBotToken(): String {
        return config.token!!
    }

    override fun onUpdateReceived(update: Update?) {
        GlobalScope.launch {
            val toUser = update!!.toUser()
            MDC.put("chat", "${toUser.chat.name}:${toUser.chat.id}")
            MDC.put("user", "${toUser.name}:${toUser.id}")
            try {
                router.processUpdate(update).invoke(this@FamilyBot).also { GlobalScope.launch { MDC.clear() } }
            } catch (e: TelegramApiRequestException) {
                log.error("Telegram error: {}, {}, {}", e.apiResponse, e.errorCode, e.parameters, e)
            } catch (e: Exception) {
                log.error("Unexpected error", e)
            }
        }
    }

    override fun getBotUsername(): String {
        return config.botname!!
    }
}
