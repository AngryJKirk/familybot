package space.yaroslav.familybot.repos.ifaces

import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.route.models.Command
import java.time.Instant


interface HistoryRepository {

    fun add(commandByUser: CommandByUser)

    fun get(user: User, from: Instant = Instant.now().minusSeconds(300),
            to: Instant = Instant.now()): List<CommandByUser>

    fun getAll(chat: Chat): List<CommandByUser>
}

data class CommandByUser(val user: User, val command: Command, val date: Instant)