package space.yaroslav.familybot.route.services.ban

import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.repos.ifaces.BanEntity
import space.yaroslav.familybot.repos.ifaces.BanEntityType
import space.yaroslav.familybot.repos.ifaces.BanRepository

@Component
class BanServiceImpl(private val banRepository: BanRepository) : BanService {
    override fun isUserBanned(user: User): Ban? {
        return banRepository.getByEntity(toEntity(user))
    }

    override fun isChatBanned(chat: Chat): Ban? {
        return banRepository.getByEntity(toEntity(chat))
    }

    override fun banUser(user: User, ban: Ban) {
        banRepository.addBan(toEntity(user), ban)
    }

    override fun banChat(chat: Chat, ban: Ban) {
        banRepository.addBan(toEntity(chat), ban)
    }

    override fun unban(ban: Ban) {
        banRepository.reduceBan(ban)
    }

    private fun toEntity(user: User) = BanEntity(user.id, BanEntityType.USER)
    private fun toEntity(chat: Chat) = BanEntity(chat.id, BanEntityType.CHAT)
}
