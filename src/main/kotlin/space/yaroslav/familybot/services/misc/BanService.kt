package space.yaroslav.familybot.services.misc

import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.repos.BanEntity
import space.yaroslav.familybot.repos.BanEntityType
import space.yaroslav.familybot.repos.BanRepository
import java.time.Instant
import java.util.UUID

@Component
class BanService(private val banRepository: BanRepository) {
    fun isUserBanned(user: User): Ban? {
        return banRepository.getByEntity(toEntity(user))
    }

    fun isChatBanned(chat: Chat): Ban? {
        return banRepository.getByEntity(toEntity(chat))
    }

    fun banUser(user: User, ban: Ban) {
        banRepository.addBan(toEntity(user), ban)
    }

    fun banChat(chat: Chat, ban: Ban) {
        banRepository.addBan(toEntity(chat), ban)
    }

    fun unban(ban: Ban) {
        banRepository.reduceBan(ban)
    }

    private fun toEntity(user: User) = BanEntity(user.id, BanEntityType.USER)
    private fun toEntity(chat: Chat) = BanEntity(chat.id, BanEntityType.CHAT)
}

data class Ban(val banId: UUID = UUID.randomUUID(), val description: String, val till: Instant)
