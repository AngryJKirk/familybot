package space.yaroslav.familybot.repos.ifaces

import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.Pidor
import space.yaroslav.familybot.common.User
import java.time.Instant


interface CommonRepository {

    fun addUser(user: User)

    fun getUsers(chat: Chat, activeOnly: Boolean = false): List<User>

    fun addChat(chat: Chat)

    fun getChats(): List<Chat>

    fun addPidor(pidor: Pidor)

    fun removePidorRecord(user: User)

    fun getPidorsByChat(chat: Chat, startDate: Instant = Instant.ofEpochMilli(969652800000),
                        endDate: Instant = Instant.now()): List<Pidor>

    fun containsUser(user: User): Boolean

    fun containsChat(chat: Chat): Boolean

    fun changeUserActiveStatus(user: User, status: Boolean)

    fun changeChatActiveStatus(chat: Chat, status: Boolean)

    fun changeUserActiveStatusNew(user: User, status: Boolean)

    fun disableUsersInChat(chat: Chat)

    fun getAllPidors(startDate: Instant = Instant.ofEpochMilli(969652800000),
                     endDate: Instant = Instant.now()): List<Pidor>
}