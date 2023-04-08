package dev.storozhenko.familybot.executors.pm

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.getLogger
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.services.misc.IgCookieService
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.services.settings.IGCookie
import dev.storozhenko.familybot.services.settings.PlainKey
import org.springframework.stereotype.Component
import org.telegram.telegrambots.facilities.filedownloader.TelegramFileDownloader
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.bots.AbsSender

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

    override fun executeInternal(context: ExecutorContext): suspend (AbsSender) -> Unit {
        return { sender ->
            val document = context.message.document
            runCatching {
                val filePath = sender.execute(GetFile(document.fileId)).filePath
                val value = downloader.downloadFile(filePath).readText()
                easyKeyValueService.put(IGCookie, IG_COOKIE_KEY, value)
                igCookieService.saveToFile(value)
                sender.send(context, "Ok")
            }.onFailure {
                sender.send(context, it.message ?: "wtf")
                getLogger().error("Bad happened during cookie upload", it)
            }
        }
    }
}