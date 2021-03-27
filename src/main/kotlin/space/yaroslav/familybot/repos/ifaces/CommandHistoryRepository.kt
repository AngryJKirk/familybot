package space.yaroslav.familybot.repos.ifaces

import io.micrometer.core.annotation.Timed
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.CommandByUser
import space.yaroslav.familybot.common.User
import java.time.Instant

interface CommandHistoryRepository {

    @Timed("CommandHistoryRepository.add")
    fun add(commandByUser: CommandByUser)

    @Timed("CommandHistoryRepository.get")
    fun get(
        user: User,
        from: Instant = Instant.now().minusSeconds(300),
        to: Instant = Instant.now()
    ): List<CommandByUser>

    @Timed("CommandHistoryRepository.getAll")
    fun getAll(chat: Chat, from: Instant? = null): List<CommandByUser>

    @Timed("CommandHistoryRepository.getTheFirst")
    fun getTheFirst(chat: Chat): CommandByUser?
}
