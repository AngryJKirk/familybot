package dev.storozhenko.familybot.feature.settings.repos

import dev.storozhenko.familybot.common.extensions.key
import dev.storozhenko.familybot.feature.settings.models.FunctionId
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.feature.settings.models.FuckOffOverride
import io.micrometer.core.annotation.Timed
import org.springframework.stereotype.Component

@Component
class FunctionsConfigureRepository(
    private val easyKeyValueService: EasyKeyValueService
) {

    private val fuckOffFunctions = listOf(
        FunctionId.CHATTING,
        FunctionId.GREETINGS,
        FunctionId.HUIFICATE,
        FunctionId.TALK_BACK
    )

    @Timed("repository.RedisFunctionsConfigureRepository.isEnabled")
    fun isEnabled(id: FunctionId, chat: Chat): Boolean {
        if (id in fuckOffFunctions &&
            easyKeyValueService.get(FuckOffOverride, chat.key()) == true
        ) {
            return false
        }
        return isEnabledInternal(id, chat)
    }

    @Timed("repository.RedisFunctionsConfigureRepository.switch")
    suspend fun switch(id: FunctionId, chat: Chat) {
        switchInternal(id, chat)
    }

    fun setStatus(id: FunctionId, chat: Chat, isEnabled: Boolean) {
        if (isEnabledInternal(id, chat) != isEnabled) {
            switchInternal(id, chat)
        }
    }

    private fun isEnabledInternal(
        id: FunctionId,
        chat: Chat
    ): Boolean {
        return easyKeyValueService.get(id.easySetting, chat.key()) ?: true
    }

    private fun switchInternal(
        id: FunctionId,
        chat: Chat
    ) {
        val key = chat.key()
        val currentValue = easyKeyValueService.get(id.easySetting, key) ?: true
        easyKeyValueService.put(id.easySetting, key, currentValue.not())
    }
}
