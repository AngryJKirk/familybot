package space.yaroslav.familybot.route.services.ban

import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import java.time.Instant
import java.util.UUID

interface BanService {

    fun isUserBanned(user: User): Ban?

    fun isChatBanned(chat: Chat): Ban?

    fun banUser(user: User, ban: Ban)

    fun banChat(chat: Chat, ban: Ban)

    fun unban(ban: Ban)
}

data class Ban(val banId: UUID = UUID.randomUUID(), val description: String, val till: Instant)
