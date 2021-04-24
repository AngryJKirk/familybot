package space.yaroslav.familybot.executors.pm

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.key
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.models.FunctionId
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.repos.PostgresFunctionsConfigureRepository
import space.yaroslav.familybot.services.settings.EasySettingsRepository
import space.yaroslav.familybot.telegram.BotConfig

@Component
class MigrationExecutor(
    private val easySettingsRepository: EasySettingsRepository,
    private val postgresFunctionsConfigureRepository: PostgresFunctionsConfigureRepository,
    private val commonRepository: CommonRepository,
    botConfig: BotConfig
) : OnlyBotOwnerExecutor(botConfig) {
    override fun getMessagePrefix() = "migrate"

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        try {
            commonRepository.getChats()
                .forEach { chat ->
                    FunctionId
                        .values()
                        .forEach { function ->
                            val isEnabled = postgresFunctionsConfigureRepository.isEnabled(function, chat)
                            easySettingsRepository.put(function.easySetting, chat.key(), isEnabled)
                        }
                }
        } catch (e: Exception) {
            return { it.send(update, "Error: $e") }
        }
        return { it.send(update, "Done") }
    }
}