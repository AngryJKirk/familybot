package space.yaroslav.familybot.executors

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Ignore
import org.springframework.beans.factory.annotation.Autowired
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.executors.command.PidorExecutor
import space.yaroslav.familybot.infrastructure.ActionWithText
import space.yaroslav.familybot.infrastructure.UpdateBuilder
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.suits.CommandExecutorTest

@Ignore(value = "Need to find out the way to bypass a new user check logic")
class PidorExecutorTest : CommandExecutorTest() {

    @Autowired
    lateinit var pidorExecutor: PidorExecutor

    @Autowired
    lateinit var commonRepository: CommonRepository

    override fun getCommandExecutor() = pidorExecutor

    override fun executeTest() {
        val update = UpdateBuilder().simpleCommandFromUser(pidorExecutor.command())
        val pidorsBefore =
            commonRepository.getPidorsByChat(update.toChat())
        val allPidors = commonRepository.getAllPidors()

        runBlocking { pidorExecutor.execute(update).invoke(testSender) }

        val firstInvokeActions = testSender.actions
        Assert.assertEquals(
            "Should always be 4 messages in case of first pidor execution of day",
            4,
            firstInvokeActions.size
        )

        val pidorsAfterFirstInvoke =
            commonRepository.getPidorsByChat(update.toChat())

        Assert.assertEquals(
            "Should be exactly one more pidor after command execute",
            pidorsBefore.size + 1,
            pidorsAfterFirstInvoke.size
        )
        Assert.assertEquals(
            "Same for all pidors in all chats",
            allPidors.size + 1,
            pidorsAfterFirstInvoke.size
        )

        val lastPidorAfterFirstInvoke =
            pidorsAfterFirstInvoke.maxByOrNull { it.date } ?: throw AssertionError("Should be one last pidor")

        val action = firstInvokeActions.last()
        Assert.assertEquals(
            "Pidor in message and in database should match",
            action.content,
            lastPidorAfterFirstInvoke.user.getGeneralName(true)
        )

        cleanSender()

        runBlocking { pidorExecutor.execute(update).invoke(testSender) }
        val secondInvokeActions = testSender.actions

        Assert.assertEquals(
            "Should always be one message in case of second pidor execution of day",
            1,
            secondInvokeActions.size
        )

        val pidorsAfterSecondInvoke =
            commonRepository.getPidorsByChat(update.toChat())

        Assert.assertEquals(
            "Should be exactly same pidors after second command execute",
            pidorsAfterFirstInvoke.size,
            pidorsAfterSecondInvoke.size
        )
        Assert.assertEquals(
            "Same for all pidors in all chats",
            allPidors.size + 1,
            pidorsAfterSecondInvoke.size
        )

        val lastPidorAfterSecondInvoke = pidorsAfterSecondInvoke
            .maxByOrNull { it.date } ?: throw AssertionError("Should be one last pidor")

        Assert.assertTrue(
            "Pidor in message and in database should match",
            firstInvokeActions
                .first()
                .let { it as ActionWithText }
                .content
                .contains(lastPidorAfterSecondInvoke.user.getGeneralName(true))
        )
    }
}
