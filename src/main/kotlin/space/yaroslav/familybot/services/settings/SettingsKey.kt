package space.yaroslav.familybot.services.settings

interface SettingsKey {
    fun value(): String
}

data class ChatSettingsKey(val chatId: Long) : SettingsKey {
    override fun value() = "$chatId:null"
}

data class UserSettingsKey(val userId: Long) : SettingsKey {
    override fun value() = "null:$userId"
}

data class UserAndChatSettingsKey(val userId: Long, val chatId: Long) : SettingsKey {
    override fun value() = "$chatId:$userId"
}
