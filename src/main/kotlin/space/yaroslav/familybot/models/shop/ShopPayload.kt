package space.yaroslav.familybot.models.shop

import kotlinx.serialization.Serializable
import space.yaroslav.familybot.services.settings.ChatEasyKey
import space.yaroslav.familybot.services.settings.UserAndChatEasyKey
import space.yaroslav.familybot.services.settings.UserEasyKey

@Serializable
data class ShopPayload(
    val chatId: Long,
    val userId: Long,
    val shopItem: ShopItem
) {

    fun chatKey(): ChatEasyKey {
        return ChatEasyKey(this.chatId)
    }

    fun userAndChatKey(): UserAndChatEasyKey {
        return UserAndChatEasyKey(this.userId, this.chatId)
    }

    fun userKey(): UserEasyKey {
        return UserEasyKey(this.userId)
    }
}
