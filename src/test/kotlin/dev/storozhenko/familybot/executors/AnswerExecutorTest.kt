package dev.storozhenko.familybot.executors

import dev.storozhenko.familybot.common.extensions.capitalized
import dev.storozhenko.familybot.feature.answer.AnswerExecutor
import dev.storozhenko.familybot.infrastructure.createSimpleCommandContext
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.suits.CommandExecutorTest
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import java.util.stream.Stream

class AnswerExecutorTest : CommandExecutorTest() {
    @Autowired
    lateinit var answerExecutor: AnswerExecutor

    @Suppress("unused")
    companion object {
        @JvmStatic
        fun answerValuesProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(listOf("первое", "второе")),
                Arguments.of(listOf("первое", "Второе")),
                Arguments.of(listOf("Первое", "второе")),
                Arguments.of(listOf("Первое", "второе", "третье")),
                Arguments.of(listOf("первое", "Второе", "третье")),
                Arguments.of(listOf("первое", "второе", "Третье"))
            )
        }
    }

    override fun getCommandExecutor() = answerExecutor

    @Ignore
    override fun executeTest() {
    }

    @ParameterizedTest
    @MethodSource("answerValuesProvider")
    fun executeParametrizedTest(values: List<String>) {
        val postfix = " " + values.joinToString(separator = " или ")
        val context = createSimpleCommandContext(Command.ANSWER, postfix = postfix)
        runBlocking { answerExecutor.execute(context).invoke(sender) }
        val sendMessageCaptor = ArgumentCaptor.forClass(SendMessage::class.java)
        verify(sender, Mockito.atLeastOnce()).execute(any<SendChatAction>())
        verify(sender, Mockito.atLeastOnce()).execute(sendMessageCaptor.capture())
        val reply = sendMessageCaptor.value.text
        Assertions.assertTrue(values.map(String::capitalized).contains(reply))
    }
}
