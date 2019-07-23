package space.yaroslav.familybot.repos.ifaces

import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.CommandByUser
import space.yaroslav.familybot.common.User
import java.time.Instant

interface CommandHistoryRepository {

    fun add(commandByUser: CommandByUser)

    fun get(
        user: User,
        from: Instant = Instant.now().minusSeconds(300),
        to: Instant = Instant.now()
    ): List<CommandByUser>

    fun getAll(chat: Chat): List<CommandByUser>
}
