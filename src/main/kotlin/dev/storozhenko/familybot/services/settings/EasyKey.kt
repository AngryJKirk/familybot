package space.yaroslav.familybot.services.settings

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
