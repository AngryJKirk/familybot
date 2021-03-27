package space.yaroslav.familybot.repos.ifaces

import io.micrometer.core.annotation.Timed
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import java.time.Instant

interface RawChatLogRepository {

    @Timed("RawChatLogRepository.add")
    fun add(chat: Chat, user: User, message: String?, fileId: String?, rawUpdate: String, date: Instant)

    @Timed("RawChatLogRepository.getMessageCount")
    fun getMessageCount(chat: Chat, user: User): Int
}
