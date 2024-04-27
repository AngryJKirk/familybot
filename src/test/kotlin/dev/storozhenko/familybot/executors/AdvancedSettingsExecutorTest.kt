package dev.storozhenko.familybot.executors

import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.keyvalue.models.ChatEasyKey
import dev.storozhenko.familybot.core.keyvalue.models.EasyKeyType
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.feature.settings.executors.AdvancedSettingsExecutor
import dev.storozhenko.familybot.feature.settings.models.TalkingDensity
import dev.storozhenko.familybot.feature.settings.models.UkrainianLanguage
import dev.storozhenko.familybot.infrastructure.createSimpleContext
import dev.storozhenko.familybot.infrastructure.randomLong
import dev.storozhenko.familybot.suits.CommandExecutorTest
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import java.util.stream.Stream

class AdvancedSettingsExecutorTest : CommandExecutorTest() {

    companion object {
        @JvmStatic
        fun valuesProvider(): Stream<Arguments> {
            val randomDensityValue = randomLong()
            return Stream.of(
                Arguments.of(
                    "${Command.ADVANCED_SETTINGS} разговорчики $randomDensityValue",
                    TalkingDensity,
                    randomDensityValue,
                ),
                Arguments.of("${Command.ADVANCED_SETTINGS} хохол вкл", UkrainianLanguage, true),
                Arguments.of("${Command.ADVANCED_SETTINGS} хохол выкл", UkrainianLanguage, false),
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
        expectedSettingValue: T,
    ) {
        val context = createSimpleContext(command)
        runBlocking { advancedSettingsExecutor.execute(context) }
        val actualSettingValue = easyKeyValueService.get(easyKeyType, context.chatKey)
        Assertions.assertEquals(expectedSettingValue, actualSettingValue)
    }
}
