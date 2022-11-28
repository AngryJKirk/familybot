package dev.storozhenko.familybot.models.shop

import com.fasterxml.jackson.annotation.JsonProperty
import dev.storozhenko.familybot.services.settings.ChatEasyKey
import dev.storozhenko.familybot.services.settings.UserAndChatEasyKey
import dev.storozhenko.familybot.services.settings.UserEasyKey

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
