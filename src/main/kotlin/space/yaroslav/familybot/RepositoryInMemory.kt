package space.yaroslav.familybot

import org.springframework.stereotype.Component

@Component
class RepositoryInMemory : Repository {
    private val usersStorage: MutableList<User> = ArrayList()

    private val chatsStorage: MutableList<Chat> = ArrayList()

    private val pidorStorage: MutableList<Pidor> = ArrayList()
    override fun addUser(user: User) {
        usersStorage.add(user)
    }
    override fun getUsers(chat: Chat): List<User> {
        return usersStorage.filter { it -> it.chat == chat }
    }

    override fun containsChat(chat: Chat): Boolean {
        return chat in chatsStorage
    }

    override fun containsUser(user: User): Boolean {
        return user in usersStorage
    }

    override fun addChat(chat: Chat) {
        chatsStorage.add(chat)
    }

    override fun getChats(): List<Chat> {
        return chatsStorage.toList()
    }

    override fun addPidor(pidor: Pidor) {
        pidorStorage.add(pidor)
    }

    override fun getPidorsByChat(chat: Chat): List<Pidor> {
        return pidorStorage.filter { it -> it.user.chat == chat }
    }
}