package dev.storozhenko.familybot.suits

import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.routers.models.Priority
import dev.storozhenko.familybot.infrastructure.createSimpleCommandContext
import dev.storozhenko.familybot.infrastructure.createSimpleContext
import dev.storozhenko.familybot.infrastructure.randomString
import org.junit.jupiter.api.Assertions

abstract class CommandExecutorTest : ExecutorTest() {

    abstract fun getCommandExecutor(): CommandExecutor

    override fun canExecuteTest() {
        val commandExecutor = getCommandExecutor()
        val command = commandExecutor.command()
        val messageWithOnlyCommand = createSimpleCommandContext(command)
        Assertions.assertTrue(
            commandExecutor.canExecute(messageWithOnlyCommand),
            "Command executor should be able to execute only if message starts with command"
        )
        val messageWithCommandInMiddle =
            createSimpleCommandContext(prefix = randomString(), postfix = randomString(), command = command)
        Assertions.assertFalse(
            commandExecutor.canExecute(messageWithCommandInMiddle),
            "Command executor should not react to command in the middle"
        )
        val messageForOtherBot = createSimpleCommandContext(command = command, postfix = "@${randomString()}")
        Assertions.assertFalse(
            commandExecutor.canExecute(messageForOtherBot),
            "Should not react for command which addressed to another bot"
        )
        val messageForSuchara = createSimpleCommandContext(command = command, postfix = "@IntegrationTests")
        Assertions.assertTrue(
            commandExecutor.canExecute(messageForSuchara),
            "Should not react for command which addressed to another bot"
        )
        val messageWithoutCommand = createSimpleContext(randomString())
        Assertions.assertFalse(
            commandExecutor.canExecute(messageWithoutCommand),
            "Any others messages should never let executor be assigned to work"
        )
    }

    override fun priorityTest() {
        Assertions.assertEquals(
            Priority.MEDIUM,
            getCommandExecutor().priority(createSimpleContext()),
            "Command executors should always have medium priority"
        )
    }
}
