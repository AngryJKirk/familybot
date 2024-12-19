package dev.storozhenko.familybot.core.telegram

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.toChat
import dev.storozhenko.familybot.common.extensions.toJson
import dev.storozhenko.familybot.common.extensions.toUser
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.keyvalue.models.ChatEasyKey
import dev.storozhenko.familybot.core.routers.PaymentRouter
import dev.storozhenko.familybot.core.routers.PollRouter
import dev.storozhenko.familybot.core.routers.ReactionsRouter
import dev.storozhenko.familybot.core.routers.Router
import dev.storozhenko.familybot.feature.settings.models.FunctionId
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.util.concurrent.ConcurrentHashMap

@Component
class FamilyBot(
    private val router: Router,
    private val pollRouter: PollRouter,
    private val paymentRouter: PaymentRouter,
    private val reactionsRouter: ReactionsRouter,
    private val easyKeyValueService: EasyKeyValueService,
    private val telegramClient: TelegramClient,
    private val botConfig: BotConfig,
    private val migrateService: MigrateService
) : LongPollingUpdateConsumer {

    private val log = KotlinLogging.logger {}
    private val routerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val channels = ConcurrentHashMap<Long, Channel<Update>>()

    override fun consume(updates: List<Update>) {
        updates
            .asSequence()
            .filterNot(Update::hasPoll)
            .forEach { update -> routerScope.launch { consumeInternal(update) } }
    }

    private suspend fun consumeInternal(update: Update) {
        if (update.hasMessage() && (update.message.migrateFromChatId != null || update.message.migrateToChatId != null)) {
            val from = update.message.migrateFromChatId ?: update.message.chatId
            val to = update.message.migrateToChatId ?: update.message.chatId
            log.info { "Migrating: $update" }
            migrateService.migrate(from, to)
        }
        if (update.hasPollAnswer()) {
            coroutineScope { launch { proceedPollAnswer(update) } }
            return
        }

        if (update.hasPreCheckoutQuery()) {
            coroutineScope { launch { proceedPreCheckoutQuery(update).invoke(telegramClient) } }
            return
        }

        if (update.message?.hasSuccessfulPayment() == true) {
            coroutineScope { launch { proceedSuccessfulPayment(update).invoke(telegramClient) } }
            return
        }

        if (update.messageReaction != null) {
            coroutineScope { launch { proceedReaction(update) } }
            return
        }

        if (update.hasCallbackQuery() &&
            update.callbackQuery.from.id == botConfig.developerId &&
            update.callbackQuery.data.startsWith("REFUND=")
        ) {
            coroutineScope { launch { proceedRefund(update).invoke(telegramClient) } }
            return
        }
        if (update.hasMessage() || update.hasCallbackQuery() || update.hasEditedMessage()) {
            val chat = update.toChat()

            val channel = channels.computeIfAbsent(chat.id) { createChannel() }

            coroutineScope { launch { channel.send(update) } }
            return
        }
    }

    private suspend fun proceed(update: Update) {
        try {
            val user = update.toUser()
            MDC.put("chat", "${user.chat.name}:${user.chat.id}")
            MDC.put("user", "${user.name}:${user.id}")
            router.processUpdate(update, telegramClient)
        } catch (e: TelegramApiRequestException) {
            val logMessage = "Telegram error: ${e.apiResponse}, ${e.errorCode}, update is ${update.toJson()}"
            if (e.errorCode in 400..499) {
                log.warn(e) { logMessage }
                if (e.apiResponse.contains("CHAT_WRITE_FORBIDDEN")) {
                    listOf(FunctionId.Chatting, FunctionId.Huificate, FunctionId.TalkBack)
                        .forEach { function ->
                            easyKeyValueService.put(
                                function,
                                ChatEasyKey(update.toChat().id),
                                false,
                            )
                        }
                }
            } else {
                log.error(e) { logMessage }
            }
        } catch (e: Exception) {
            log.error(e) { "Unexpected error, update is ${update.toJson()}" }
        } finally {
            MDC.clear()
        }
    }

    private fun proceedReaction(update: Update) {
        runCatching {
            reactionsRouter.proceed(update.messageReaction)
        }.onFailure {
            log.error(it) { "reactionRouter.proceed failed" }
        }
    }

    private fun proceedPollAnswer(update: Update) {
        runCatching {
            pollRouter.proceed(update)
        }.onFailure {
            log.warn(it) { "pollRouter.proceed failed" }
        }
    }

    private fun proceedPreCheckoutQuery(update: Update): suspend (TelegramClient) -> Unit {
        return runCatching {
            paymentRouter.proceedPreCheckoutQuery(update)
        }.onFailure {
            log.error(it) { "paymentRouter.proceedPreCheckoutQuery failed" }
        }.getOrDefault { }
    }

    private fun proceedSuccessfulPayment(update: Update): suspend (TelegramClient) -> Unit {
        return runCatching {
            paymentRouter.proceedSuccessfulPayment(update)
        }.onFailure {
            log.warn(it) { "paymentRouter.proceedSuccessfulPayment failed" }
        }.getOrDefault { }
    }

    private fun proceedRefund(update: Update): suspend (TelegramClient) -> Unit {
        return runCatching {
            paymentRouter.proceedRefund(update)
        }.onFailure {
            log.warn(it) { "paymentRouter.proceedRefund failed" }
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
