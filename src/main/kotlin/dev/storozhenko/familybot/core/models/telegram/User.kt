package dev.storozhenko.familybot.core.models.telegram

import dev.storozhenko.familybot.common.extensions.link

data class User(val id: Long, val chat: Chat, val name: String?, val nickname: String?) {

    fun getGeneralName(mention: Boolean = true): String {
        return if (mention) {
            if (nickname != null) {
                "@$nickname"
            } else {
                name?.link("tg://user?id=$id") ?: "хуй знает кто"
            }
        } else {
            name ?: "хуй знает кто"
        }
    }
}
