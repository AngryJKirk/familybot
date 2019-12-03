package space.yaroslav.familybot.repos.ifaces

import java.time.Instant
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User

interface RawChatLogRepository {

    fun add(chat: Chat, user: User, message: String?, fileId: String?, rawUpdate: String, date: Instant)

    fun getMessageCount(chat: Chat, user: User): Int
}
