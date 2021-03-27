package space.yaroslav.familybot.repos.ifaces

import io.micrometer.core.annotation.Timed
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.models.FunctionId

interface FunctionsConfigureRepository {

    @Timed("FunctionsConfigureRepository.isEnabled")
    fun isEnabled(id: FunctionId, chat: Chat): Boolean

    @Timed("FunctionsConfigureRepository.switch")
    fun switch(id: FunctionId, chat: Chat)
}
