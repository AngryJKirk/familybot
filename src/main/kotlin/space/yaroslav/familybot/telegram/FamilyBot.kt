package space.yaroslav.familybot.telegram

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import space.yaroslav.familybot.common.extensions.toChat
import space.yaroslav.familybot.common.extensions.toUser
import space.yaroslav.familybot.services.routers.PaymentRouter
import space.yaroslav.familybot.services.routers.PollRouter
import space.yaroslav.familybot.services.routers.Router

@Component
class FamilyBot(
    val config: BotConfig,
    val router: Router,
    val pollRouter: PollRouter,
    val paymentRouter: PaymentRouter
) : TelegramLongPollingBot() {

    private val log = LoggerFactory.getLogger(FamilyBot::class.java)
    private val routerScope = CoroutineScope(Dispatchers.Default)
    private val channels = HashMap<Long, Channel<Update>>()

    override fun getBotToken(): String {
        return config.token ?: throw InternalException("Expression 'config.token' must not be null")
    }

    override fun onUpdateReceived(tgUpdate: Update?) {

        val update = tgUpdate ?: throw InternalException("Update should not be null")
        if (update.hasPollAnswer()) {
            routerScope.launch { proceedPollAnswer(update) }
            return
        }

        if (update.hasPreCheckoutQuery()) {
            routerScope.launch { proceedPreCheckoutQuery(update).invoke(this@FamilyBot) }
            return
        }

        if (update.message?.hasSuccessfulPayment() == true) {
            routerScope.launch { proceedSuccessfulPayment(update).invoke(this@FamilyBot) }
            return
        }

        if (update.hasPoll()) {
            return
        }
        if (update.hasMessage() || update.hasCallbackQuery() || update.hasEditedMessage()) {
            val chat = update.toChat()

            val channel = channels.computeIfAbsent(chat.id) { createChannel() }

            routerScope.launch { channel.send(update) }
        }
    }

    override fun getBotUsername(): String {
        return config.botname ?: throw InternalException("Expression 'config.botname' must not be null")
    }

    private suspend fun proceed(update: Update) {
        val user = update.toUser()
        MDC.put("chat", "${user.chat.name}:${user.chat.id}")
        MDC.put("user", "${user.name}:${user.id}")
        try {
            router.processUpdate(update).invoke(this@FamilyBot)
        } catch (e: TelegramApiRequestException) {
            val logMessage = "Telegram error: ${e.apiResponse}, ${e.errorCode}, update is $update"
            if (e.errorCode in 400..499) {
                log.warn(logMessage, e)
            } else {
                log.error(logMessage, e)
            }
        } catch (e: Exception) {
            log.error("Unexpected error, update is $update", e)
        } finally {
            MDC.clear()
        }
    }

    private fun proceedPollAnswer(update: Update) {
        runCatching {
            pollRouter.proceed(update)
        }.onFailure {
            log.warn("Poll router failed", it)
        }
    }

    private fun proceedPreCheckoutQuery(update: Update): suspend (AbsSender) -> Unit {
        return runCatching {
            paymentRouter.proceedPreCheckoutQuery(update)
        }.getOrDefault {
            log.warn("Poll router failed", it) {}
        }
    }

    private fun proceedSuccessfulPayment(update: Update): suspend (AbsSender) -> Unit {
        return runCatching {
            paymentRouter.proceedSuccessfulPayment(update)
        }.getOrDefault {
            log.warn("Poll router failed", it) {}
        }
    }

    private fun createChannel(): Channel<Update> {
        val channel = Channel<Update>()
        routerScope.launch {
            for (incomingUpdate in channel) {
                proceed(incomingUpdate)
            }
        }
        return channel
    }

    class InternalException(override val message: String?) : RuntimeException(message)
}
