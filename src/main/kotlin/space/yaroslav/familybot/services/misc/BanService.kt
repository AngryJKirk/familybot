package space.yaroslav.familybot.services.misc

import org.springframework.stereotype.Component
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.models.telegram.User
import space.yaroslav.familybot.common.utils.key
import space.yaroslav.familybot.common.utils.prettyFormat
import space.yaroslav.familybot.services.settings.Ban
import space.yaroslav.familybot.services.settings.EasyKey
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import java.time.Duration
import java.time.Instant

@Component
class BanService(
    private val easyKeyValueService: EasyKeyValueService
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

    fun findBanByKey(easyKey: EasyKey): String? {
        return easyKeyValueService.get(
            Ban,
            easyKey
        )
    }

    fun reduceBan(easyKey: EasyKey) {
        easyKeyValueService.remove(Ban, easyKey)
    }

    private fun banByKey(easyKey: EasyKey, description: String) {
        val duration = Duration.ofDays(7)
        val until = Instant.now().plusSeconds(duration.seconds)
        easyKeyValueService.put(
            Ban,
            easyKey,
            "Бан нахуй по причине \"$description\" до ${until.prettyFormat()}",
            duration = duration
        )
    }
}
