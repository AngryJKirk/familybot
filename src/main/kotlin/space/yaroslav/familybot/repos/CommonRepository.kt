package space.yaroslav.familybot.repos

import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.Pidor
import space.yaroslav.familybot.common.User


interface CommonRepository {

    fun addUser(user: User)

    fun getUsers(chat: Chat): List<User>

    fun addChat(chat: Chat)

    fun getChats(): List<Chat>

    fun addPidor(pidor: Pidor)

    fun getPidorsByChat(chat: Chat): List<Pidor>

    fun containsUser(user: User): Boolean

    fun containsChat(chat: Chat): Boolean
}