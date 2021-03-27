package space.yaroslav.familybot.repos.ifaces

import io.micrometer.core.annotation.Timed
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User

interface ChatLogRepository {

    @Timed("ChatLogRepository.add")
    fun add(user: User, message: String)

    @Timed("ChatLogRepository.get")
    fun get(user: User): List<String>

    @Timed("ChatLogRepository.getAll")
    fun getAll(): List<String>

    @Timed("ChatLogRepository.getAllByChat")
    fun getAllByChat(chat: Chat): List<String>
}
