package dev.storozhenko.familybot.executors

import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import dev.storozhenko.familybot.executors.command.settings.AdvancedSettingsExecutor
import dev.storozhenko.familybot.infrastructure.createSimpleContext
import dev.storozhenko.familybot.infrastructure.randomLong
import dev.storozhenko.familybot.models.telegram.Command
import dev.storozhenko.familybot.services.settings.ChatEasyKey
import dev.storozhenko.familybot.services.settings.EasyKeyType
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.services.settings.TalkingDensity
import dev.storozhenko.familybot.services.settings.UkrainianLanguage
import dev.storozhenko.familybot.suits.CommandExecutorTest
import java.util.stream.Stream

class AdvancedSettingsExecutorTest : CommandExecutorTest() {

    @Suppress("unused")
    companion object {
        @JvmStatic
        fun valuesProvider(): Stream<Arguments> {
            val randomDensityValue = randomLong()
            return Stream.of(
                Arguments.of(
                    "${Command.ADVANCED_SETTINGS} разговорчики $randomDensityValue",
                    TalkingDensity,
                    randomDensityValue
                ),
                Arguments.of("${Command.ADVANCED_SETTINGS} хохол вкл", UkrainianLanguage, true),
                Arguments.of("${Command.ADVANCED_SETTINGS} хохол выкл", UkrainianLanguage, false)
            )
        }
    }

    @Autowired
    lateinit var advancedSettingsExecutor: AdvancedSettingsExecutor

    @Autowired
    lateinit var easyKeyValueService: EasyKeyValueService

    override fun getCommandExecutor() = advancedSettingsExecutor

    @Ignore
    override fun executeTest() {
    }

    @ParameterizedTest
    @MethodSource("valuesProvider")
    fun <T : Any> executeTest(
        command: String,
        easyKeyType: EasyKeyType<T, ChatEasyKey>,
        expectedSettingValue: T
    ) {
        val context = createSimpleContext(command)
        runBlocking { advancedSettingsExecutor.execute(context).invoke(sender) }
        val actualSettingValue = easyKeyValueService.get(easyKeyType, context.chatKey)
        Assertions.assertEquals(expectedSettingValue, actualSettingValue)
    }
}
