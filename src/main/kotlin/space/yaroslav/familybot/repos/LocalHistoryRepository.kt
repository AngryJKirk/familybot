package space.yaroslav.familybot.repos

import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.repos.ifaces.CommandByUser
import space.yaroslav.familybot.repos.ifaces.HistoryRepository
import java.time.Instant

@Component
class LocalHistoryRepository : HistoryRepository {

    val storage = ArrayList<CommandByUser>()

    override fun add(commandByUser: CommandByUser) {
        storage.add(commandByUser)
    }

    override fun get(user: User, from: Instant, to: Instant): List<CommandByUser> {
        return storage
                .filter { it.user == user }
                .filter { it.date.isAfter(from) and it.date.isBefore(to) }
    }
}