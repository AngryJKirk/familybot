package dev.storozhenko.familybot.telegram

import dev.storozhenko.familybot.common.extensions.toChat
import dev.storozhenko.familybot.common.extensions.toJson
import dev.storozhenko.familybot.common.extensions.toUser
import dev.storozhenko.familybot.services.routers.PaymentRouter
import dev.storozhenko.familybot.services.routers.PollRouter
import dev.storozhenko.familybot.services.routers.Router
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

@Component
class FamilyBot(
    val config: BotConfig,
    val router: Router,
    val pollRouter: PollRouter,
    val paymentRouter: PaymentRouter
) : TelegramLongPollingBot(config.botToken) {

    private val log = LoggerFactory.getLogger(FamilyBot::class.java)
    private val routerScope = CoroutineScope(Dispatchers.Default)
    private val channels = HashMap<Long, Channel<Update>>()

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
        return config.botName
    }

    private suspend fun proceed(update: Update) {
        try {
            val user = update.toUser()
            MDC.put("chat", "${user.chat.name}:${user.chat.id}")
            MDC.put("user", "${user.name}:${user.id}")
            router.processUpdate(update).invoke(this@FamilyBot)
        } catch (e: TelegramApiRequestException) {
            val logMessage = "Telegram error: ${e.apiResponse}, ${e.errorCode}, update is ${update.toJson()}"
            if (e.errorCode in 400..499) {
                log.warn(logMessage, e)
            } else {
                log.error(logMessage, e)
            }
        } catch (e: Exception) {
            log.error("Unexpected error, update is ${update.toJson()}", e)
        } finally {
            MDC.clear()
        }
    }

    private fun proceedPollAnswer(update: Update) {
        runCatching {
            pollRouter.proceed(update)
        }.onFailure {
            log.warn("pollRouter.proceed failed", it)
        }
    }

    private fun proceedPreCheckoutQuery(update: Update): suspend (AbsSender) -> Unit {
        return runCatching {
            paymentRouter.proceedPreCheckoutQuery(update)
        }.onFailure {
            log.error("paymentRouter.proceedPreCheckoutQuery failed", it)
        }.getOrDefault { }
    }

    private fun proceedSuccessfulPayment(update: Update): suspend (AbsSender) -> Unit {
        return runCatching {
            paymentRouter.proceedSuccessfulPayment(update)
        }.onFailure {
            log.warn("paymentRouter.proceedSuccessfulPayment failed", it)
        }.getOrDefault { }
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

    class InternalException(override val message: String) : RuntimeException(message)
}
