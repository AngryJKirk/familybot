package dev.storozhenko.familybot.feature.shop.model

import com.fasterxml.jackson.annotation.JsonProperty
import dev.storozhenko.familybot.core.keyvalue.models.ChatEasyKey
import dev.storozhenko.familybot.core.keyvalue.models.UserAndChatEasyKey
import dev.storozhenko.familybot.core.keyvalue.models.UserEasyKey

data class ShopPayload(
    @JsonProperty("chatId") val chatId: Long,
    @JsonProperty("userId") val userId: Long,
    @JsonProperty("shopItem") val shopItem: ShopItem,
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
