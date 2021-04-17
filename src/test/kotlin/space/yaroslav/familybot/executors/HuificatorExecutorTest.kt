package space.yaroslav.familybot.executors

import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.common.utils.key
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.executors.eventbased.HuificatorExecutor
import space.yaroslav.familybot.infrastructure.createSimpleUpdate
import space.yaroslav.familybot.models.Priority
import space.yaroslav.familybot.services.settings.EasySettingsService
import space.yaroslav.familybot.services.settings.TalkingDensity
import space.yaroslav.familybot.suits.ExecutorTest
import java.util.stream.Stream

class HuificatorExecutorTest : ExecutorTest() {

    @Suppress("unused")
    companion object {
        @JvmStatic
        fun valuesProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("штош", null),
                Arguments.of("как-нибудь", "хуяк-нибудь"),
                Arguments.of("дерево", "хуерево"),
                Arguments.of("hello", null),
                Arguments.of("куку шуку пидор", "хуидор"),
                Arguments.of("куку шуку             петух", "хуетух"),
                Arguments.of("удача", "хуюдача"),
                Arguments.of("х-у-й-ня", null)
            )
        }
    }

    @Autowired
    lateinit var huificatorExecutor: HuificatorExecutor

    @Autowired
    lateinit var easySettingsService: EasySettingsService

    override fun priorityTest() {
        val update = Update()
        val priority = huificatorExecutor.priority(update)
        Assertions.assertEquals(Priority.RANDOM, priority, "Huificator executor should be random")
    }

    override fun canExecuteTest() {
        val canExecute = huificatorExecutor.canExecute(Message())
        Assertions.assertFalse(canExecute, "Should always be not available to execute")
    }

    @Ignore
    override fun executeTest() {
    }

    @ParameterizedTest
    @MethodSource("valuesProvider")
    fun executeTest(input: String, expected: String?) {
        val update = createSimpleUpdate(input)
        easySettingsService.put(TalkingDensity, update.toChat().key(), 0)
        val sender = testSender.sender
        runBlocking {
            huificatorExecutor.execute(update).invoke(sender)
        }
        if (expected == null) {
            verifyNoInteractions(sender)
        } else {
            val sendMessageCaptor = ArgumentCaptor.forClass(SendMessage::class.java)
            verify(sender, atLeastOnce()).execute(any<SendChatAction>())
            verify(sender, atLeastOnce()).execute(sendMessageCaptor.capture())
            Assertions.assertEquals(expected, sendMessageCaptor.value.text)
        }
    }
}
