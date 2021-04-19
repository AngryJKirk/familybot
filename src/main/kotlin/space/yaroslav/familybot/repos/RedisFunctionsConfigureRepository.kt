package space.yaroslav.familybot.repos

import io.micrometer.core.annotation.Timed
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.utils.key
import space.yaroslav.familybot.models.FunctionId
import space.yaroslav.familybot.services.settings.`EasySettingsRepository.kt`

@Component
@Primary
class RedisFunctionsConfigureRepository(
    private val settingsRepository: `EasySettingsRepository.kt`,
    private val oldRepository: PostgresFunctionsConfigureRepository
) : FunctionsConfigureRepository {

    @Timed("repository.RedisFunctionsConfigureRepository.isEnabled")
    override fun isEnabled(id: FunctionId, chat: Chat): Boolean {
        val redisFunctionState = settingsRepository.get(id.easySetting, chat.key())
        return if (redisFunctionState == null) {
            val postgresFunctionState = oldRepository.isEnabled(id, chat)
            settingsRepository.put(id.easySetting, chat.key(), postgresFunctionState)
            postgresFunctionState
        } else {
            redisFunctionState
        }
    }

    @Timed("repository.RedisFunctionsConfigureRepository.switch")
    override suspend fun switch(id: FunctionId, chat: Chat) {
        val currentValue = settingsRepository.get(id.easySetting, chat.key()) ?: true
        settingsRepository.put(id.easySetting, chat.key(), currentValue.not())
        coroutineScope { launch { oldRepository.switch(id, chat) } }
    }
}
