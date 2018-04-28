package space.yaroslav.familybot.repos.ifaces

import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.CustomMessage

interface CustomMessageDeliveryRepository {

    fun hasNewMessages(chat: Chat): Boolean

    fun getNewMessages(chat: Chat): List<CustomMessage>

    fun markAsDelivered(message: CustomMessage)
}