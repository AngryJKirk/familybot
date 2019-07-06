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
        return config.token ?: throw InternalException("Expression 'config.token' must not be null")
    }

    override fun onUpdateReceived(update: Update?) {
        if (update == null) {
            throw InternalException("Update should not be null")
        }
        GlobalScope.launch {
            val toUser = update.toUser()
            MDC.put("chat", "${toUser.chat.name}:${toUser.chat.id}")
            MDC.put("user", "${toUser.name}:${toUser.id}")
            try {
                router.processUpdate(update).invoke(this@FamilyBot).also { MDC.clear() }
            } catch (e: TelegramApiRequestException) {
                log.error("Telegram error: {}, {}, {}", e.apiResponse, e.errorCode, e.parameters, e)
            } catch (e: Exception) {
                log.error("Unexpected error", e)
            }
        }
    }

    override fun getBotUsername(): String {
        return config.botname ?: throw InternalException("Expression 'config.botname' must not be null")
    }

    class InternalException(override val message: String?) : RuntimeException(message)
}

