package space.yaroslav.familybot.executors

import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import space.yaroslav.familybot.executors.command.settings.AdvancedSettingsExecutor
import space.yaroslav.familybot.infrastructure.createSimpleContext
import space.yaroslav.familybot.infrastructure.randomLong
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.services.settings.*
import space.yaroslav.familybot.suits.CommandExecutorTest
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
