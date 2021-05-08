package space.yaroslav.familybot.executors

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.executors.eventbased.keyword.KeyWordExecutor
import space.yaroslav.familybot.infrastructure.createSimpleMessage
import space.yaroslav.familybot.infrastructure.createSimpleUpdate
import space.yaroslav.familybot.infrastructure.createSimpleUser
import space.yaroslav.familybot.infrastructure.randomString
import space.yaroslav.familybot.models.router.Priority
import space.yaroslav.familybot.suits.ExecutorTest
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ReplyToUserExecutorTest : ExecutorTest() {

    @Autowired
    lateinit var replyToUserExecutor: KeyWordExecutor

    @Value("\${settings.botname}")
    lateinit var botName: String

    override fun priorityTest() {
        val actual = replyToUserExecutor.priority(Update())
        Assertions.assertEquals(Priority.LOW, actual)
    }

    override fun canExecuteTest() {
        val update = updateWithReplyToBotMessage()
        val canExecute = replyToUserExecutor.canExecute(update.message)
        Assertions.assertTrue { canExecute }
    }

    override fun executeTest() {
        val update = updateWithReplyToBotMessage()
        runBlocking {
            replyToUserExecutor.canExecute(update.message)
            replyToUserExecutor.execute(update).invoke(sender)
        }
        argumentCaptor<SendMessage> {
            verify(sender, times(2)).execute(capture())
            assertNotNull("Should be reply") { secondValue.replyToMessageId }
            assertTrue("Reply should not be empty") { secondValue.text.isNotEmpty() }
        }
    }

    private fun updateWithReplyToBotMessage(): Update {
        return createSimpleUpdate(randomString())
            .apply {
                message.replyToMessage = createSimpleMessage(text = randomString(), chat = message.chat)
                message.replyToMessage.from = createSimpleUser(isBot = true, botName = botName)
            }
    }
}
