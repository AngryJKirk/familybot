package space.yaroslav.familybot.common

import space.yaroslav.familybot.route.models.Command
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

data class Chat(val id: Long, val name: String?)

data class Pidor(val user: User, val date: Instant)

data class CommandByUser(val user: User, val command: Command, val date: Instant)

enum class Pluralization(val code: Int) {
    ONE(1),
    FEW(2),
    MANY(3);

    companion object PluralizationCalc {
        fun getPlur(position: Int): Pluralization {
            return if (position % 10 == 1 && position % 100 != 11) ONE
            else if (position % 10 in 2..4 && (position % 100 < 10 || position % 100 >= 20)) FEW
            else MANY
        }
    }

}