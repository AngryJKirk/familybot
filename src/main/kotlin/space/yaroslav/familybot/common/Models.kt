package space.yaroslav.familybot.common

import java.time.Instant


data class User(val id: Long, val chat: Chat, val name: String?, val nickname: String?) {

    fun getGeneralName(mention: Boolean = true): String {
        var mentionString = ""
        if(mention){
            mentionString = "@"
        }
        return nickname?.let { mentionString + it } ?: name ?: "Хуй знает кто"
    }
}

class Chat(val id: Long, val name: String?){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Chat) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Chat(id=$id, name=$name)"
    }


}

data class Pidor(val user: User, val date: Instant)


