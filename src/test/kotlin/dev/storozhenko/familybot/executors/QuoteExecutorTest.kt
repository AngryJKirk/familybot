package dev.storozhenko.familybot.executors

import dev.storozhenko.familybot.feature.tribute.executors.QuoteExecutor
import dev.storozhenko.familybot.infrastructure.createSimpleCommandContext
import dev.storozhenko.familybot.suits.CommandExecutorTest
import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

class QuoteExecutorTest : CommandExecutorTest() {

    @Autowired
    lateinit var quoteExecutorTest: QuoteExecutor

    override fun getCommandExecutor() = quoteExecutorTest

    override fun executeTest() {
        val update = createSimpleCommandContext(quoteExecutorTest.command())
        runBlocking { quoteExecutorTest.execute(update).invoke(sender) }
        verify(sender, times(1)).execute(any<SendMessage>())
    }
}
