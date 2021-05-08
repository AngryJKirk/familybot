package space.yaroslav.familybot.executors

import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.common.extensions.toChat
import space.yaroslav.familybot.executors.command.settings.AdvancedSettingsExecutor
import space.yaroslav.familybot.infrastructure.createSimpleUpdate
import space.yaroslav.familybot.infrastructure.randomLong
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.services.settings.ChatEasyKey
import space.yaroslav.familybot.services.settings.EasyKeyType
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.TalkingDensity
import space.yaroslav.familybot.services.settings.UkrainianLanguage
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
        val update = createSimpleUpdate(command)
        runBlocking { advancedSettingsExecutor.execute(update).invoke(sender) }
        val actualSettingValue = easyKeyValueService.get(easyKeyType, update.toChat().key())
        Assertions.assertEquals(expectedSettingValue, actualSettingValue)
    }
}
