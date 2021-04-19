package space.yaroslav.familybot.repos

import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.models.FunctionId

interface FunctionsConfigureRepository {

    fun isEnabled(id: FunctionId, chat: Chat): Boolean

    suspend fun switch(id: FunctionId, chat: Chat)

    suspend fun setStatus(id: FunctionId, chat: Chat, isEnabled: Boolean)
}
