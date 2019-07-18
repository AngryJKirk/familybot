package space.yaroslav.familybot.executors

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.springframework.beans.factory.annotation.Autowired
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.infrastructure.UpdateBuilder
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.route.executors.command.PidorExecutor
import space.yaroslav.familybot.suits.CommandExecutorTest

class PidorExecutorTest : CommandExecutorTest() {

    @Autowired
    lateinit var pidorExecutor: PidorExecutor

    @Autowired
    lateinit var commonRepository: CommonRepository

    override fun getCommandExecutor() = pidorExecutor

    override fun executeTest() {
        val update = UpdateBuilder().simpleTextMessageFromUser(pidorExecutor.command().command)
        val pidorsBefore =
            commonRepository.getPidorsByChat(update.toChat())
        val allPidors = commonRepository.getAllPidors()
        
        runBlocking { pidorExecutor.execute(update).invoke(testSender) }
        val actions = testSender.actions
        Assert.assertEquals("Should always be 4 messages in case of first pidor execution of day", 4, actions.size)

        val pidorsAfter =
            commonRepository.getPidorsByChat(update.toChat())

        Assert.assertEquals(
            "Should be exactly one more pidor after command execute",
            pidorsBefore.size + 1,
            pidorsAfter.size
        )
        Assert.assertEquals(
            "Same for all pidors in all chats",
            allPidors.size + 1,
            pidorsAfter.size
        )

        val lastPidor = pidorsAfter.maxBy { it.date } ?: throw AssertionError("Should be one last pidor")

        Assert.assertEquals(
            "Pidor in message and in database should match",
            actions.last().text,
            lastPidor.user.getGeneralName(true)
        )
    }
}
