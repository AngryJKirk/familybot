package space.yaroslav.familybot.repos.ifaces

import space.yaroslav.familybot.common.Chat

interface RagemodeRepository {

    fun enable(minutes: Int, messages: Int, chat: Chat)

    fun isEnabled(chat: Chat): Boolean

    fun decrement(chat: Chat)
}