package space.yaroslav.familybot.executors

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Ignore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.common.utils.key
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.executors.pm.BanSomeoneExecutor
import space.yaroslav.familybot.infrastructure.createSimpleUpdate
import space.yaroslav.familybot.infrastructure.randomString
import space.yaroslav.familybot.models.Priority
import space.yaroslav.familybot.services.misc.BanService
import space.yaroslav.familybot.services.settings.EasyKey
import space.yaroslav.familybot.suits.ExecutorTest
import space.yaroslav.familybot.telegram.BotConfig
import space.yaroslav.familybot.telegram.FamilyBot
import java.util.stream.Stream

class BanSomeoneExecutorTest : ExecutorTest() {
    @Suppress("unused")
    companion object {
        @JvmStatic
        fun valuesProvider(): Stream<Arguments> {
            val update = createSimpleUpdate()
            val chatToBan = update.toChat()
            val userToBan = update.toUser()
            return Stream.of(
                Arguments.of(BanTestModel(userToBan.name, userToBan.key())),
                Arguments.of(BanTestModel(userToBan.nickname, userToBan.key())),
                Arguments.of(BanTestModel(userToBan.id.toString(), userToBan.key())),
                Arguments.of(BanTestModel(chatToBan.id.toString(), chatToBan.key())),
                Arguments.of(BanTestModel(chatToBan.name, chatToBan.key()))
            )
        }
    }

    @Autowired
    lateinit var banSomeoneExecutor: BanSomeoneExecutor

    @Autowired
    lateinit var botConfig: BotConfig

    @Autowired
    lateinit var banService: BanService

    override fun priorityTest() {
        val priority = banSomeoneExecutor.priority(Update())
        Assertions.assertEquals(Priority.HIGH, priority)
    }

    override fun canExecuteTest() {
        val validMessage = updateFromDeveloper(banSomeoneExecutor.getMessagePrefix()).message

        val canExecuteValid = banSomeoneExecutor.canExecute(validMessage)

        Assert.assertTrue(canExecuteValid)

        val notValidMessage = updateFromDeveloper(randomString()).message

        val canExecuteNotValid = banSomeoneExecutor.canExecute(notValidMessage)

        Assertions.assertFalse(canExecuteNotValid)
    }

    @Ignore
    override fun executeTest() {
    }

    @TestFactory
    @MethodSource("valuesProvider")
    fun executeTest(banModel: BanTestModel) {
        clearInvocations(sender)
        val description = randomString()
        val update = updateFromDeveloper("${banSomeoneExecutor.getMessagePrefix()}${banModel.key}|$description")
        runBlocking { banSomeoneExecutor.execute(update).invoke(sender) }
        verify(sender).execute(any<SendMessage>())

        banService.findBanByKey(banModel.easyKey)
            ?: throw AssertionError("Should be a new ban")

        banService.reduceBan(banModel.easyKey)
    }

    @Test
    fun `user should be banned without chat`() {
        val update = createSimpleUpdate()
        val user = update.toUser()
        val chat = update.toChat()
        val description = randomString()
        banService.banUser(user, description)
        Assertions.assertTrue(banService.isUserBanned(user)?.contains(description) ?: false)
        Assertions.assertTrue(banService.isChatBanned(chat) == null)
        Assertions.assertTrue(banService.findBanByKey(update.key()) == null)
        Assertions.assertTrue(banService.findBanByKey(chat.key()) == null)
        Assertions.assertTrue(banService.findBanByKey(user.key()) != null)
        banService.reduceBan(user.key())
        banService.banChat(chat, description)
        Assertions.assertTrue(banService.isChatBanned(chat)?.contains(description) ?: false)
        Assertions.assertTrue(banService.isUserBanned(user) == null)
        Assertions.assertTrue(banService.findBanByKey(update.key()) == null)
        Assertions.assertTrue(banService.findBanByKey(chat.key()) != null)
        Assertions.assertTrue(banService.findBanByKey(user.key()) == null)
        banService.reduceBan(user.key())
    }

    private fun updateFromDeveloper(messageText: String): Update {
        val developerUsername =
            botConfig.developer ?: throw FamilyBot.InternalException("Someone wrong with tests setup")
        return createSimpleUpdate(messageText).apply {
            message.from.userName = developerUsername
            message.chat.apply {
                type = "private"
                userName = developerUsername
            }
        }
    }

    data class BanTestModel(
        val key: String?,
        val easyKey: EasyKey
    )
}
