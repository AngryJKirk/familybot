package space.yaroslav.familybot.suits

import org.junit.Before
import org.junit.Test
import space.yaroslav.familybot.infrastructure.TestSender

abstract class ExecutorTest : FamilybotApplicationTest() {

    protected val testSender = TestSender()

    @Before
    fun cleanSender() {
        testSender.actions.removeAll { true }
    }

    @Test
    abstract fun priotityTest()

    @Test
    abstract fun canExecuteTest()

    @Test
    abstract fun executeTest()
}
