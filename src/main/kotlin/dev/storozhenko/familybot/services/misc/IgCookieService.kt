package dev.storozhenko.familybot.services.misc

import dev.storozhenko.familybot.executors.pm.IgCookiesExecutor
import dev.storozhenko.familybot.getLogger
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.services.settings.IGCookie
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.io.File

@Component
class IgCookieService(private val easyKeyValueService: EasyKeyValueService) {

    private val log = getLogger()

    @EventListener(ApplicationReadyEvent::class)
    fun init() {
        val cookieValue = easyKeyValueService.get(IGCookie, IgCookiesExecutor.IG_COOKIE_KEY) ?: return
        saveToFile(cookieValue)
        log.info("cookies.txt is set, absolute path is ${getPath()}")
    }

    fun saveToFile(value: String) {
        val appDataDir = File(System.getProperty("user.home"), ".family")
        if (!appDataDir.exists()) {
            appDataDir.mkdirs()
        }

        val dataFile = File(appDataDir, "cookies.txt")
        dataFile.writeText(value)
    }

    fun getPath(): String? {
        val appDataDir = File(System.getProperty("user.home"), ".family")
        if (!appDataDir.exists()) {
            return null
        }

        val dataFile = File(appDataDir, "cookies.txt")
        if (!dataFile.exists()) {
            return null
        }
        return dataFile.absolutePath
    }
}