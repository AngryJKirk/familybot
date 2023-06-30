package dev.storozhenko.familybot.executors

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority
import dev.storozhenko.familybot.feature.talking.executors.KeyWordExecutor
import dev.storozhenko.familybot.infrastructure.createSimpleContext
import dev.storozhenko.familybot.infrastructure.createSimpleMessage
import dev.storozhenko.familybot.infrastructure.createSimpleUser
import dev.storozhenko.familybot.infrastructure.randomString
import dev.storozhenko.familybot.suits.ExecutorTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import java.io.Serializable
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ReplyToUserExecutorTest : ExecutorTest() {

    @Autowired
    lateinit var replyToUserExecutor: KeyWordExecutor

    @Autowired
    lateinit var botConfig: BotConfig

    override fun priorityTest() {
        val actual = replyToUserExecutor.priority(createSimpleContext())
        Assertions.assertEquals(Priority.VERY_LOW, actual)
    }

    override fun canExecuteTest() {
        val context = updateWithReplyToBotMessage()
        val canExecute = replyToUserExecutor.canExecute(context)
        Assertions.assertTrue { canExecute }
    }

    override fun executeTest() {
        val update = updateWithReplyToBotMessage()
        runBlocking {
            replyToUserExecutor.canExecute(update)
            replyToUserExecutor.execute(update)
        }
        argumentCaptor<BotApiMethod<Serializable>> {
            verify(sender, times(2)).execute(capture())
            val sentMessage = secondValue as SendMessage
            assertNotNull("Should be reply") { sentMessage.replyToMessageId }
            assertTrue("Reply should not be empty") { sentMessage.text.isNotEmpty() }
        }
    }

    private fun updateWithReplyToBotMessage(): ExecutorContext {
        return createSimpleContext(randomString())
            .apply {
                message.replyToMessage = createSimpleMessage(text = randomString(), chat = message.chat)
                message.replyToMessage.from = createSimpleUser(isBot = true, botName = botConfig.botName)
            }
    }
}
