package dev.storozhenko.familybot.feature.tiktok.executors

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.OnlyBotOwnerExecutor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.keyvalue.models.PlainKey
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.IGCookie
import dev.storozhenko.familybot.feature.tiktok.services.IgCookieService
import dev.storozhenko.familybot.getLogger
import org.springframework.stereotype.Component
import org.telegram.telegrambots.facilities.filedownloader.TelegramFileDownloader
import org.telegram.telegrambots.meta.api.methods.GetFile

@Component
class IgCookiesExecutor(
    private val easyKeyValueService: EasyKeyValueService,
    private val igCookieService: IgCookieService,
    private val downloader: TelegramFileDownloader
) :
    OnlyBotOwnerExecutor() {
    companion object {
        val IG_COOKIE_KEY = PlainKey("IG_COOKIE_KEY")
    }

    override fun getMessagePrefix() = "cookies.txt"

    override fun canExecute(context: ExecutorContext): Boolean {
        if (context.isFromDeveloper.not()) return false
        val document = context.message.document ?: return false
        return document.fileName == getMessagePrefix()
    }

    override suspend fun executeInternal(context: ExecutorContext) {
        val document = context.message.document
        runCatching {
            val filePath = context.sender.execute(GetFile(document.fileId)).filePath
            val value = downloader.downloadFile(filePath).readText()
            easyKeyValueService.put(IGCookie, IG_COOKIE_KEY, value)
            igCookieService.saveToFile(value)
            context.sender.send(context, "Ok")
        }.onFailure {
            context.sender.send(context, it.message ?: "wtf")
            getLogger().error("Bad happened during cookie upload", it)
        }
    }
}
