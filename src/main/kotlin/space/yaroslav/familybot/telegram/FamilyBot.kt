package space.yaroslav.familybot.telegram

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.services.PollRouter
import space.yaroslav.familybot.services.Router

@Component
class FamilyBot(
    val config: BotConfig,
    val router: Router,
    val pollRouter: PollRouter
) : TelegramLongPollingBot() {

    private final val log = LoggerFactory.getLogger(FamilyBot::class.java)
    private final val channels = HashMap<Long, Channel<Update>>()

    override fun getBotToken(): String {
        return config.token ?: throw InternalException("Expression 'config.token' must not be null")
    }

    override fun onUpdateReceived(tgUpdate: Update?) {
        val update = tgUpdate ?: throw InternalException("Update should not be null")
        if (update.hasPollAnswer()) {
            GlobalScope.launch { proceedPollAnswer(update) }
            return
        }
        if (update.hasPoll()) {
            return
        }

        val chat = update.toChat()

        val channel = channels.computeIfAbsent(chat.id) {
            Channel<Update>()
                .also { GlobalScope.launch { for (incomingUpdate in it) proceed(incomingUpdate) } }
        }
        GlobalScope.launch { channel.send(update) }
    }

    override fun getBotUsername(): String {
        return config.botname ?: throw InternalException("Expression 'config.botname' must not be null")
    }

    private suspend fun proceed(update: Update) {
        val user = update.toUser()
        MDC.put("chat", "${user.chat.name}:${user.chat.id}")
        MDC.put("user", "${user.name}:${user.id}")
        MDC.put("update_id", update.updateId.toString())
        try {
            router.processUpdate(update).invoke(this@FamilyBot)
        } catch (e: TelegramApiRequestException) {
            log.error("Telegram error: ${e.apiResponse}, ${e.errorCode}, ${e.parameters}, update is $update", e)
        } catch (e: Exception) {
            log.error("Unexpected error, update is $update", e)
        }.also { MDC.clear() }
    }

    private suspend fun proceedPollAnswer(update: Update) {
        pollRouter.proceed(update)
    }

    class InternalException(override val message: String?) : RuntimeException(message)
}
