package dev.storozhenko.familybot.suits

import dev.storozhenko.familybot.infrastructure.TestSender
import org.junit.jupiter.api.Test

abstract class ExecutorTest : FamilybotApplicationTest() {

    val sender = TestSender().sender

    @Test
    fun runPriorityTest() {
        priorityTest()
    }

    @Test
    fun runCanExecuteTest() {
        canExecuteTest()
    }

    @Test
    fun runExecuteTest() {
        executeTest()
    }

    abstract fun priorityTest()

    abstract fun canExecuteTest()

    abstract fun executeTest()
}
