package space.yaroslav.familybot.services.misc

import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.key
import space.yaroslav.familybot.common.utils.prettyFormat
import space.yaroslav.familybot.services.settings.Ban
import space.yaroslav.familybot.services.settings.EasySettingsService
import space.yaroslav.familybot.services.settings.SettingsKey
import java.time.Duration
import java.time.Instant

@Component
class BanService(
    private val easySettingsService: EasySettingsService
) {

    fun isUserBanned(user: User): String? {
        return findBanByKey(user.key())
    }

    fun isChatBanned(chat: Chat): String? {
        return findBanByKey(chat.key())
    }

    fun banUser(user: User, description: String) {
        banByKey(user.key(), description)
    }

    fun banChat(chat: Chat, description: String) {
        banByKey(chat.key(), description)
    }

    fun findBanByKey(settingsKey: SettingsKey): String? {
        return easySettingsService.get(
            Ban,
            settingsKey
        )
    }

    fun reduceBan(settingsKey: SettingsKey) {
        easySettingsService.remove(Ban, settingsKey)
    }

    private fun banByKey(settingsKey: SettingsKey, description: String) {
        val duration = Duration.ofDays(7)
        val until = Instant.now().plusSeconds(duration.seconds)
        easySettingsService.put(
            Ban,
            settingsKey,
            "Бан нахуй по причине \"$description\" до ${until.prettyFormat()}",
            duration = duration
        )
    }
}
