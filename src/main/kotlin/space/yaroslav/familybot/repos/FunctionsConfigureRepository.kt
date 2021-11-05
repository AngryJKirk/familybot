package space.yaroslav.familybot.repos

import io.micrometer.core.annotation.Timed
import org.springframework.stereotype.Component
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.FunctionId
import space.yaroslav.familybot.services.settings.EasyKeyValueRepository
import space.yaroslav.familybot.services.settings.FuckOffOverride

@Component
class FunctionsConfigureRepository(
    private val keyValueRepository: EasyKeyValueRepository
) {

    private val fuckOffFunctions = listOf(
        FunctionId.CHATTING,
        FunctionId.GREETINGS,
        FunctionId.HUIFICATE,
        FunctionId.TALK_BACK
    )

    @Timed("repository.RedisFunctionsConfigureRepository.isEnabled")
    fun isEnabled(id: FunctionId, context: ExecutorContext): Boolean {
        if (id in fuckOffFunctions &&
            keyValueRepository.get(FuckOffOverride, context.chatKey) == true
        ) {
            return false
        }
        return isEnabledInternal(id, context)
    }

    @Timed("repository.RedisFunctionsConfigureRepository.switch")
    suspend fun switch(id: FunctionId, context: ExecutorContext) {
        switchInternal(id, context)
    }

    fun setStatus(id: FunctionId, context: ExecutorContext, isEnabled: Boolean) {
        if (isEnabledInternal(id, context) != isEnabled) {
            switchInternal(id, context)
        }
    }

    private fun isEnabledInternal(
        id: FunctionId,
        context: ExecutorContext
    ): Boolean {
        return keyValueRepository.get(id.easySetting, context.chatKey) ?: true
    }

    private fun switchInternal(
        id: FunctionId,
        context: ExecutorContext
    ) {
        val key = context.chatKey
        val currentValue = keyValueRepository.get(id.easySetting, key) ?: true
        keyValueRepository.put(id.easySetting, key, currentValue.not())
    }
}
