package space.yaroslav.familybot.models

import com.fasterxml.jackson.annotation.JsonProperty
import space.yaroslav.familybot.services.settings.ChatEasyKey
import space.yaroslav.familybot.services.settings.UserAndChatEasyKey
import space.yaroslav.familybot.services.settings.UserEasyKey

data class ShopPayload(
    @JsonProperty("chatId") val chatId: Long,
    @JsonProperty("userId") val userId: Long,
    @JsonProperty("shopItem") val shopItem: ShopItem
)

fun ShopPayload.chatKey(): ChatEasyKey {
    return ChatEasyKey(this.chatId)
}

fun ShopPayload.userAndChatKey(): UserAndChatEasyKey {
    return UserAndChatEasyKey(this.userId, this.chatId)
}

fun ShopPayload.userKey(): UserEasyKey {
    return UserEasyKey(this.userId)
}