package dev.storozhenko.familybot.services.settings

interface EasyKey {
    fun value(): String
}

data class ChatEasyKey(val chatId: Long) : EasyKey {
    override fun value() = "$chatId:null"
}

data class UserEasyKey(val userId: Long) : EasyKey {
    override fun value() = "null:$userId"
}

data class UserAndChatEasyKey(val userId: Long, val chatId: Long) : EasyKey {
    override fun value() = "$chatId:$userId"
}

data class PlainKey(val key: String) : EasyKey {
    companion object {
        const val PREFIX = "plain_key"
    }
    override fun value() = "$PREFIX:$key"
}
