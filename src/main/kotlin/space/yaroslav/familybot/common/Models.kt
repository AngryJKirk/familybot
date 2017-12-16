package space.yaroslav.familybot.common

import java.time.LocalDateTime


data class User(val id: Long, val chat: Chat, val name: String?, val nickname: String?) {

    fun getGeneralName(mention: Boolean = true): String {
        var mentionString = ""
        if(mention){
            mentionString = "@"
        }
        return nickname?.let { mentionString + it } ?: name ?: "Хуй знает кто"
    }
}

data class Chat(val id: Long, val name: String?)

data class Pidor(val user: User, val date: LocalDateTime)


