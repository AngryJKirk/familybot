package space.yaroslav.familybot.executors

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.springframework.beans.factory.annotation.Autowired
import space.yaroslav.familybot.infrastructure.UpdateBuilder
import space.yaroslav.familybot.route.executors.command.QuoteExecutor
import space.yaroslav.familybot.suits.CommandExecutorTest

class QuoteExecutorTest : CommandExecutorTest() {

    @Autowired
    lateinit var quoteExecutorTest: QuoteExecutor

    override fun getCommandExecutor() = quoteExecutorTest

    override fun executeTest() {
        val update = UpdateBuilder().simpleTextMessageFromUser(quoteExecutorTest.command().command)
        runBlocking { quoteExecutorTest.execute(update).invoke(testSender) }
        val actions = testSender.actions
        Assert.assertTrue("Should be exactly one action with quote", actions.size == 1)
    }
}
