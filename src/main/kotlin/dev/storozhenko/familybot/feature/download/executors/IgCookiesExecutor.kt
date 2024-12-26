package dev.storozhenko.familybot.feature.download.executors


import dev.storozhenko.familybot.core.executors.OnlyBotOwnerExecutor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.keyvalue.models.PlainKey
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.download.services.IgCookieService
import dev.storozhenko.familybot.feature.settings.models.IGCookie
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.generics.TelegramClient

@Component
class IgCookiesExecutor(
    private val easyKeyValueService: EasyKeyValueService,
    private val igCookieService: IgCookieService,
    private val telegramClient: TelegramClient,
) :
    OnlyBotOwnerExecutor() {
    companion object {
        val IG_COOKIE_KEY = PlainKey("IG_COOKIE_KEY")
    }

    private val log = KotlinLogging.logger { }

    override fun getMessagePrefix() = "cookies.txt"

    override fun canExecute(context: ExecutorContext): Boolean {
        if (context.isFromDeveloper.not()) return false
        val document = context.message.document ?: return false
        return document.fileName == getMessagePrefix()
    }

    override suspend fun executeInternal(context: ExecutorContext) {
        val document = context.message.document
        runCatching {
            val filePath = context.client.execute(GetFile(document.fileId)).filePath
            val value = telegramClient.downloadFile(filePath).readText()
            easyKeyValueService.put(IGCookie, IG_COOKIE_KEY, value)
            igCookieService.saveToFile(value)
            context.send("Ok")
        }.onFailure {
            context.send(it.message ?: "wtf")
                log.error(it) { "Bad happened during cookie upload" }
        }
    }
}
