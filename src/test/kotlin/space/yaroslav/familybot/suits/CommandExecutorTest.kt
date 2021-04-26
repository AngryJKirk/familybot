package space.yaroslav.familybot.suits

import org.junit.jupiter.api.Assertions
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.infrastructure.createSimpleCommand
import space.yaroslav.familybot.infrastructure.createSimpleMessage
import space.yaroslav.familybot.infrastructure.randomString
import space.yaroslav.familybot.models.Priority

abstract class CommandExecutorTest : ExecutorTest() {

    abstract fun getCommandExecutor(): CommandExecutor

    override fun canExecuteTest() {
        val commandExecutor = getCommandExecutor()
        val command = commandExecutor.command()
        val messageWithOnlyCommand = createSimpleCommand(command)
        Assertions.assertTrue(
            commandExecutor.canExecute(messageWithOnlyCommand.message),
            "Command executor should be able to execute only if message starts with command"
        )
        val messageWithCommandInMiddle =
            createSimpleCommand(prefix = randomString(), postfix = randomString(), command = command)
        Assertions.assertFalse(
            commandExecutor.canExecute(messageWithCommandInMiddle.message),
            "Command executor should not react to command in the middle"
        )
        val messageForOtherBot = createSimpleCommand(command = command, postfix = "@${randomString()}")
        Assertions.assertFalse(
            commandExecutor.canExecute(messageForOtherBot.message),
            "Should not react for command which addressed to another bot"
        )
        val messageForSuchara = createSimpleCommand(command = command, postfix = "@IntegrationTests")
        Assertions.assertTrue(
            commandExecutor.canExecute(messageForSuchara.message),
            "Should not react for command which addressed to another bot"
        )
        val messageWithoutCommand = createSimpleMessage(randomString())
        Assertions.assertFalse(
            commandExecutor.canExecute(messageWithoutCommand),
            "Any others messages should never let executor be assigned to work"
        )
    }

    override fun priorityTest() {
        Assertions.assertEquals(
            Priority.MEDIUM,
            getCommandExecutor().priority(Update()),
            "Command executors should always have medium priority"
        )
    }
}
