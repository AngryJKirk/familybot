package space.yaroslav.familybot.executors

import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import space.yaroslav.familybot.executors.command.QuoteExecutor
import space.yaroslav.familybot.infrastructure.createSimpleCommand
import space.yaroslav.familybot.suits.CommandExecutorTest

class QuoteExecutorTest : CommandExecutorTest() {

    @Autowired
    lateinit var quoteExecutorTest: QuoteExecutor

    override fun getCommandExecutor() = quoteExecutorTest

    override fun executeTest() {
        val update = createSimpleCommand(quoteExecutorTest.command())
        runBlocking { quoteExecutorTest.execute(update).invoke(sender) }
        verify(sender, times(1)).execute(any<SendMessage>())
    }
}
