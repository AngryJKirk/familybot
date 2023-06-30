package dev.storozhenko.familybot.executors

import dev.storozhenko.familybot.feature.pidor.executors.PidorExecutor
import dev.storozhenko.familybot.feature.pidor.repos.PidorRepository
import dev.storozhenko.familybot.infrastructure.createSimpleCommandContext
import dev.storozhenko.familybot.suits.CommandExecutorTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.StringRedisTemplate
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import java.io.Serializable

class PidorExecutorTest : CommandExecutorTest() {

    @Autowired
    lateinit var pidorExecutor: PidorExecutor

    @Autowired
    lateinit var pidorRepository: PidorRepository

    @Autowired
    lateinit var redisTemplate: StringRedisTemplate

    override fun getCommandExecutor() = pidorExecutor

    override fun executeTest() {
        val context = createSimpleCommandContext(pidorExecutor.command())
        val pidorsBefore =
            pidorRepository.getPidorsByChat(context.chat)
        val allPidors = pidorRepository.getAllPidors()

        runBlocking { pidorExecutor.execute(context) }
        val captor = argumentCaptor<BotApiMethod<Serializable>> {
            verify(sender, times(11)).execute(capture())
        }

        val pidorsAfterFirstInvoke =
            pidorRepository.getPidorsByChat(context.chat)
        Assertions.assertEquals(
            pidorsBefore.size + 1,
            pidorsAfterFirstInvoke.size,
            "Should be exactly one more pidor after command execute",
        )
        Assertions.assertEquals(
            allPidors.size + 1,
            pidorsAfterFirstInvoke.size,
            "Same for all pidors in all chats",
        )

        val lastPidorAfterFirstInvoke = pidorsAfterFirstInvoke.maxByOrNull { it.date }
            ?: throw AssertionError("Should be one last pidor")

        val firstPidorName = captor.allValues.last() as SendMessage
        Assertions.assertEquals(
            firstPidorName.text,
            lastPidorAfterFirstInvoke.user.getGeneralName(true),
            "Pidor in message and in database should match",
        )
        runBlocking { pidorExecutor.execute(context) }
        argumentCaptor<BotApiMethod<Serializable>> {
            verify(sender, times(12)).execute(capture())
        }

        val pidorsAfterSecondInvoke =
            pidorRepository.getPidorsByChat(context.chat)

        Assertions.assertEquals(
            pidorsAfterFirstInvoke.size,
            pidorsAfterSecondInvoke.size,
            "Should be exactly same pidors after second command execute",
        )
        Assertions.assertEquals(
            allPidors.size + 1,
            pidorsAfterSecondInvoke.size,
            "Same for all pidors in all chats",
        )

        val lastPidorAfterSecondInvoke = pidorsAfterSecondInvoke
            .maxByOrNull { it.date } ?: throw AssertionError("Should be one last pidor")

        Assertions.assertTrue(
            firstPidorName.text.contains(lastPidorAfterSecondInvoke.user.getGeneralName(true)),
            "Pidor in message and in database should match",
        )
        pidorRepository.getAllPidors().forEach { (user) ->
            pidorRepository.removePidorRecord(user)
        }

        redisTemplate.delete(redisTemplate.keys("*"))
    }
}
