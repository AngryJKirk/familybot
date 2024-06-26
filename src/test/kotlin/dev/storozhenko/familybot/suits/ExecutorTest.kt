package dev.storozhenko.familybot.suits

import dev.storozhenko.familybot.infrastructure.TestClient
import org.junit.jupiter.api.Test

abstract class ExecutorTest : FamilybotApplicationTest() {

    val client = TestClient().client

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
