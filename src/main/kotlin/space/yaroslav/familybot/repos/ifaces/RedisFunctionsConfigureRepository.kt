package space.yaroslav.familybot.repos.ifaces

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.utils.key
import space.yaroslav.familybot.models.FunctionId
import space.yaroslav.familybot.repos.PostgresFunctionsConfigureRepository
import space.yaroslav.familybot.services.settings.EasySettingsRedisRepository

@Component
@Primary
class RedisFunctionsConfigureRepository(
    private val settingsRepository: EasySettingsRedisRepository,
    private val oldRepository: PostgresFunctionsConfigureRepository
) : FunctionsConfigureRepository {

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

    override fun switch(id: FunctionId, chat: Chat) {
        val currentValue = settingsRepository.get(id.easySetting, chat.key()) ?: true
        settingsRepository.put(id.easySetting, chat.key(), currentValue.not())
    }
}
