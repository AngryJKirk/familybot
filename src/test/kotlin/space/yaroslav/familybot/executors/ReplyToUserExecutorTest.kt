package space.yaroslav.familybot.executors

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.executors.eventbased.keyword.KeyWordExecutor
import space.yaroslav.familybot.infrastructure.ActionWithText
import space.yaroslav.familybot.infrastructure.ChatBuilder
import space.yaroslav.familybot.infrastructure.UpdateBuilder
import space.yaroslav.familybot.infrastructure.UserBuilder
import space.yaroslav.familybot.models.Priority
import space.yaroslav.familybot.suits.ExecutorTest

class ReplyToUserExecutorTest : ExecutorTest() {

    @Autowired
    lateinit var replyToUserExecutor: KeyWordExecutor

    @Value("\${settings.botname}")
    lateinit var botName: String

    override fun priotityTest() {
        val actual = replyToUserExecutor.priority(Update())
        Assert.assertEquals(Priority.VERY_LOW, actual)
    }

    override fun canExecuteTest() {
        val update = updateWithReplyToBotMessage()
        val canExecute = replyToUserExecutor.canExecute(update.message)
        Assert.assertTrue(canExecute)
    }

    override fun executeTest() {
        val update = updateWithReplyToBotMessage()
        runBlocking { replyToUserExecutor.execute(update).invoke(testSender) }
        Assert.assertTrue("Should be at least one action", testSender.actions.isNotEmpty())
        Assert.assertTrue("Should be reply", testSender.actions.first().replyId != null)
        Assert.assertTrue(
            "Reply should not be empty", testSender.actions.first()
                .let { it as ActionWithText }.content.isNotBlank()
        )
    }

    private fun updateWithReplyToBotMessage(): Update {
        val commonChat = ChatBuilder()
        return UpdateBuilder()
            .message {
                chat { commonChat }
                from { UserBuilder() }
                to {
                    chat { commonChat }
                    from { UserBuilder().toBot(botName) }
                }
            }.build()
    }
}
