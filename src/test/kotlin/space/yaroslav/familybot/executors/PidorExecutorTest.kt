package space.yaroslav.familybot.executors

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.StringRedisTemplate
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import space.yaroslav.familybot.common.extensions.toChat
import space.yaroslav.familybot.executors.command.PidorExecutor
import space.yaroslav.familybot.infrastructure.createSimpleCommand
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.suits.CommandExecutorTest

class PidorExecutorTest : CommandExecutorTest() {

    @Autowired
    lateinit var pidorExecutor: PidorExecutor

    @Autowired
    lateinit var commonRepository: CommonRepository

    @Autowired
    lateinit var redisTemplate: StringRedisTemplate

    override fun getCommandExecutor() = pidorExecutor

    override fun executeTest() {
        val update = createSimpleCommand(pidorExecutor.command())
        val pidorsBefore =
            commonRepository.getPidorsByChat(update.toChat())
        val allPidors = commonRepository.getAllPidors()

        runBlocking { pidorExecutor.execute(update).invoke(sender) }
        val firstCaptor = ArgumentCaptor.forClass(SendMessage::class.java)
        verify(sender, times(11)).execute(firstCaptor.capture())

        val pidorsAfterFirstInvoke =
            commonRepository.getPidorsByChat(update.toChat())

        Assertions.assertEquals(
            pidorsBefore.size + 1,
            pidorsAfterFirstInvoke.size,
            "Should be exactly one more pidor after command execute",
        )
        Assertions.assertEquals(
            allPidors.size + 1,
            pidorsAfterFirstInvoke.size,
            "Same for all pidors in all chats"
        )

        val lastPidorAfterFirstInvoke = pidorsAfterFirstInvoke.maxByOrNull { it.date }
            ?: throw AssertionError("Should be one last pidor")

        val firstPidorName = firstCaptor.allValues.last()
        Assertions.assertEquals(
            firstPidorName.text,
            lastPidorAfterFirstInvoke.user.getGeneralName(true),
            "Pidor in message and in database should match"
        )
        val secondCaptor = ArgumentCaptor.forClass(SendMessage::class.java)
        runBlocking { pidorExecutor.execute(update).invoke(sender) }
        verify(sender, times(12)).execute(secondCaptor.capture())

        val pidorsAfterSecondInvoke =
            commonRepository.getPidorsByChat(update.toChat())

        Assertions.assertEquals(
            pidorsAfterFirstInvoke.size,
            pidorsAfterSecondInvoke.size,
            "Should be exactly same pidors after second command execute"
        )
        Assertions.assertEquals(
            allPidors.size + 1,
            pidorsAfterSecondInvoke.size,
            "Same for all pidors in all chats"
        )

        val lastPidorAfterSecondInvoke = pidorsAfterSecondInvoke
            .maxByOrNull { it.date } ?: throw AssertionError("Should be one last pidor")

        Assertions.assertTrue(
            firstPidorName.text.contains(lastPidorAfterSecondInvoke.user.getGeneralName(true)),
            "Pidor in message and in database should match"
        )
        commonRepository.getAllPidors().forEach { pidor ->
            commonRepository.removePidorRecord(pidor.user)
        }

        redisTemplate.delete(redisTemplate.keys("*"))
    }
}
