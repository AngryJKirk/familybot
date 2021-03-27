package space.yaroslav.familybot.repos.ifaces

import io.micrometer.core.annotation.Timed
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.Pidor
import space.yaroslav.familybot.common.User
import java.time.Instant

interface CommonRepository {

    @Timed("CommonRepository.addUser")
    fun addUser(user: User)

    @Timed("CommonRepository.getUsers")
    fun getUsers(chat: Chat, activeOnly: Boolean = false): List<User>

    @Timed("CommonRepository.addChat")
    fun addChat(chat: Chat)

    @Timed("CommonRepository.getChats")
    fun getChats(): List<Chat>

    @Timed("CommonRepository.addPidor")
    fun addPidor(pidor: Pidor)

    @Timed("CommonRepository.removePidorRecord")
    fun removePidorRecord(user: User)

    @Timed("CommonRepository.getPidorsByChat")
    fun getPidorsByChat(
        chat: Chat,
        startDate: Instant = Instant.ofEpochMilli(969652800000),
        endDate: Instant = Instant.now()
    ): List<Pidor>

    @Timed("CommonRepository.containsUser")
    fun containsUser(user: User): Boolean

    @Timed("CommonRepository.containsChat")
    fun containsChat(chat: Chat): Boolean

    @Timed("CommonRepository.changeUserActiveStatus")
    fun changeUserActiveStatus(user: User, status: Boolean)

    @Timed("CommonRepository.changeChatActiveStatus")
    fun changeChatActiveStatus(chat: Chat, status: Boolean)

    @Timed("CommonRepository.changeUserActiveStatusNew")
    fun changeUserActiveStatusNew(user: User, status: Boolean)

    @Timed("CommonRepository.disableUsersInChat")
    fun disableUsersInChat(chat: Chat)

    @Timed("CommonRepository.getAllPidors")
    fun getAllPidors(
        startDate: Instant = Instant.ofEpochMilli(969652800000),
        endDate: Instant = Instant.now()
    ): List<Pidor>
}
