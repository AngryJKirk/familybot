package space.yaroslav.familybot.models.shop

import com.fasterxml.jackson.annotation.JsonProperty
import space.yaroslav.familybot.services.settings.ChatEasyKey
import space.yaroslav.familybot.services.settings.UserAndChatEasyKey
import space.yaroslav.familybot.services.settings.UserEasyKey

data class ShopPayload(
    @JsonProperty("chatId") val chatId: Long,
    @JsonProperty("userId") val userId: Long,
    @JsonProperty("shopItem") val shopItem: ShopItem
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
