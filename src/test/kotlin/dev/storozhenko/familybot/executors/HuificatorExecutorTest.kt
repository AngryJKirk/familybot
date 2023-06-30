package dev.storozhenko.familybot.executors

import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.routers.models.Priority
import dev.storozhenko.familybot.feature.settings.models.TalkingDensity
import dev.storozhenko.familybot.feature.talking.executors.HuificatorExecutor
import dev.storozhenko.familybot.infrastructure.createSimpleContext
import dev.storozhenko.familybot.suits.ExecutorTest
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
                Arguments.of("х-у-й-ня", null),
                Arguments.of("ножик", "хуёжик"),
                Arguments.of("пхпхпх", "хуепхпх"),
            )
        }
    }

    @Autowired
    lateinit var huificatorExecutor: HuificatorExecutor

    @Autowired
    lateinit var easyKeyValueService: EasyKeyValueService

    override fun priorityTest() {
        val priority = huificatorExecutor.priority(createSimpleContext())
        Assertions.assertEquals(Priority.RANDOM, priority, "Huificator executor should be random")
    }

    override fun canExecuteTest() {
        val canExecute = huificatorExecutor.canExecute(createSimpleContext())
        Assertions.assertFalse(canExecute, "Should always be not available to execute")
    }

    @Ignore
    override fun executeTest() {
    }

    @ParameterizedTest
    @MethodSource("valuesProvider")
    fun executeTest(input: String, expected: String?) {
        val context = createSimpleContext(input)
        easyKeyValueService.put(TalkingDensity, context.chatKey, 0)
        runBlocking {
            huificatorExecutor.execute(context)
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
