package dev.storozhenko.familybot.core.models.telegram

data class User(val id: Long, val chat: Chat, val name: String?, val nickname: String?) {

    fun getGeneralName(mention: Boolean = true): String {
        return if (mention) {
            if (nickname != null) {
                "@$nickname"
            } else {
                "<a href=\"F$id\">$name</a>"
            }
        } else {
            name ?: "хуй знает кто"
        }
    }
}
