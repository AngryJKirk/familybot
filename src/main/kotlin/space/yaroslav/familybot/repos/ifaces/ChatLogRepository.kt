package space.yaroslav.familybot.repos.ifaces

import space.yaroslav.familybot.common.User


interface ChatLogRepository{

    fun add(user: User, message: String)

    fun get(user: User): List<String>

}