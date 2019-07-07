package space.yaroslav.familybot.suits

import org.junit.Assert
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.infrastructure.UpdateBuilder
import space.yaroslav.familybot.infrastructure.randomUUID
import space.yaroslav.familybot.route.executors.command.CommandExecutor
import space.yaroslav.familybot.route.models.Priority

abstract class CommandExecutorTest : ExecutorTest() {

    abstract fun getCommandExecutor(): CommandExecutor

    override fun canExecuteTest() {
        val commandExecutor = getCommandExecutor()
        val command = commandExecutor.command().command
        val messageWithOnlyCommand = UpdateBuilder().simpleTextMessageFromUser(command)
        Assert.assertTrue(
            "Command executor should be able to execute only if message starts with command",
            commandExecutor.canExecute(messageWithOnlyCommand.message)
        )
        val messageWithCommandInMiddle = UpdateBuilder()
            .simpleTextMessageFromUser(randomUUID() + command + randomUUID())
        Assert.assertFalse(
            "Command executor should not react to command in the middle",
            commandExecutor.canExecute(messageWithCommandInMiddle.message)
        )
        val messageWithoutCommand = UpdateBuilder().simpleTextMessageFromUser(randomUUID())
        Assert.assertFalse(
            "Any others messages should never let executor be assigned to work",
            commandExecutor.canExecute(messageWithoutCommand.message)
        )
    }

    override fun priotityTest() {
        Assert.assertEquals(
            "Command executors should always have medium priority",
            Priority.MEDIUM,
            getCommandExecutor().priority(Update())
        )
    }
}
