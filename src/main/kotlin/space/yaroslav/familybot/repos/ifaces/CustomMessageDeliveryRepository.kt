package space.yaroslav.familybot.repos.ifaces

import io.micrometer.core.annotation.Timed
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.CustomMessage

interface CustomMessageDeliveryRepository {

    @Timed("CustomMessageDeliveryRepository.hasNewMessages")
    fun hasNewMessages(chat: Chat): Boolean

    @Timed("CustomMessageDeliveryRepository.getNewMessages")
    fun getNewMessages(chat: Chat): List<CustomMessage>

    @Timed("CustomMessageDeliveryRepository.markAsDelivered")
    fun markAsDelivered(message: CustomMessage)
}
