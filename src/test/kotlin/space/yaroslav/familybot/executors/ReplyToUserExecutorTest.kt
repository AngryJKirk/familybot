package space.yaroslav.familybot.executors

import org.junit.Assert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.infrastructure.ChatBuilder
import space.yaroslav.familybot.infrastructure.MessageBuilder
import space.yaroslav.familybot.infrastructure.UpdateBuilder
import space.yaroslav.familybot.infrastructure.UserBuilder
import space.yaroslav.familybot.route.executors.eventbased.ReplyToUserExecutor
import space.yaroslav.familybot.route.models.Priority
import space.yaroslav.familybot.suits.ExecutorTest

class ReplyToUserExecutorTest : ExecutorTest() {

    @Autowired
    lateinit var replyToUserExecutor: ReplyToUserExecutor

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
        replyToUserExecutor.execute(update).invoke(testSender)
        Assert.assertTrue("Should be at least one action", testSender.actions.isNotEmpty())
        Assert.assertTrue("Should be reply", testSender.actions.first().replyId != null)
        Assert.assertTrue("Reply should not be empty", testSender.actions.first().text.isNotBlank())
    }

    private fun updateWithReplyToBotMessage(): Update {
        val chat = ChatBuilder()
        return UpdateBuilder()
            .addMessage(
                MessageBuilder()
                    .addChat(chat)
                    .addFrom(UserBuilder())
                    .addRepledMessage(
                        MessageBuilder()
                            .addFrom(
                                UserBuilder()
                                    .toBot(botName)
                            )
                            .addChat(chat)
                    )
            ).build()
    }
}
