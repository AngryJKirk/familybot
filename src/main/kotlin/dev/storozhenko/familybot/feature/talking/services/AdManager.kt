package dev.storozhenko.familybot.feature.talking.services

import dev.storozhenko.familybot.common.extensions.readFileFromStatic
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.executors.Executor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.AdCooldown
import dev.storozhenko.familybot.feature.settings.models.ChatGPTPaidTill
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import java.time.Duration
import java.time.Instant

@Component
class AdManager(
    private val easyKeyValueService: EasyKeyValueService,
) {
    private val log = KotlinLogging.logger {}
    private val ads: List<Ad> = init()


    suspend fun promote(executor: Executor, context: ExecutorContext) {
        if (ads.isEmpty()) {
            return
        }
        if (executor !is CommandExecutor) {
            return
        }
        val paidTill = easyKeyValueService.get(ChatGPTPaidTill, context.chatKey, Instant.MIN)

        if (paidTill.isAfter(Instant.now())) {
            return
        }

        val promoteCooldownReset = easyKeyValueService.get(AdCooldown, context.chatKey, Instant.MIN)

        if (promoteCooldownReset.isBefore(Instant.now())) {
            sendPromoteMessage(context)
            easyKeyValueService.put(AdCooldown, context.chatKey, Instant.now().plus(Duration.ofDays(2)))
        }
    }

    private suspend fun sendPromoteMessage(context: ExecutorContext) {
        delay(3000)
        val ad = ads.random()
        log.info { "Promoting ${ad.adUrl} for chat ${context.chat}" }
        context.send(ad.message, enableHtml = true, customization = {
            disableWebPagePreview()
            replyMarkup = InlineKeyboardMarkup(listOf(InlineKeyboardRow(InlineKeyboardButton(ad.adButtonText).apply {
                url = ad.adUrl
            })))
        })
    }

    private fun init(): List<Ad> {
        val rawAds = readFileFromStatic("ads.txt")
        val rawAdLinks = readFileFromStatic("ads_link.txt")
        return if (rawAds == null || rawAdLinks == null) {
            emptyList()
        } else {
            log.info { "Ads enabled" }
            val (adLink, adButtonText) = rawAdLinks.lines()
            rawAds.split("====")
                .map { message -> Ad(message, adLink, adButtonText) }
        }
    }

}

data class Ad(
    val message: String,
    val adUrl: String,
    val adButtonText: String,
)