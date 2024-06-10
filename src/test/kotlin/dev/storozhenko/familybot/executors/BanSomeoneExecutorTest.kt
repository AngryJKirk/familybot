package dev.storozhenko.familybot.executors

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.key
import dev.storozhenko.familybot.common.extensions.toChat
import dev.storozhenko.familybot.common.extensions.toUser
import dev.storozhenko.familybot.core.keyvalue.models.EasyKey
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority
import dev.storozhenko.familybot.feature.ban.executors.BanSomeoneExecutor
import dev.storozhenko.familybot.feature.ban.services.BanService
import dev.storozhenko.familybot.infrastructure.createSimpleContext
import dev.storozhenko.familybot.infrastructure.createSimpleUpdate
import dev.storozhenko.familybot.infrastructure.randomString
import dev.storozhenko.familybot.suits.ExecutorTest
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import java.util.stream.Stream

class BanSomeoneExecutorTest : ExecutorTest() {
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
                Arguments.of(BanTestModel(chatToBan.name, chatToBan.key())),
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
        val priority = banSomeoneExecutor.priority(createSimpleContext())
        Assertions.assertEquals(Priority.HIGH, priority)
    }

    override fun canExecuteTest() {
        val validContext = updateFromDeveloper(banSomeoneExecutor.getMessagePrefix())

        val canExecuteValid = banSomeoneExecutor.canExecute(validContext)

        Assertions.assertTrue(canExecuteValid)

        val notValidContext = updateFromDeveloper(randomString())

        val canExecuteNotValid = banSomeoneExecutor.canExecute(notValidContext)

        Assertions.assertFalse(canExecuteNotValid)
    }

    @Ignore
    override fun executeTest() {
    }

    @ParameterizedTest
    @MethodSource("valuesProvider")
    fun executeTest(banModel: BanTestModel) {
        clearInvocations(client)
        val description = randomString()
        val update = updateFromDeveloper("${banSomeoneExecutor.getMessagePrefix()}${banModel.key}|$description")
        runBlocking { banSomeoneExecutor.execute(update) }
        verify(client).execute(any<SendMessage>())

        banService.findBanByKey(banModel.easyKey)
            ?: throw AssertionError("Should be a new ban")

        banService.removeBan(banModel.easyKey)
    }

    @Test
    fun `user should be banned without chat`() {
        val context = createSimpleContext()
        val user = context.user
        val chat = context.chat
        val description = randomString()
        banService.banUser(user, description)
        Assertions.assertTrue(banService.getUserBan(context)?.contains(description) ?: false)
        Assertions.assertNull(banService.getChatBan(context))
        Assertions.assertNull(banService.findBanByKey(context.userAndChatKey))
        Assertions.assertNull(banService.findBanByKey(chat.key()))
        Assertions.assertNotNull(banService.findBanByKey(user.key()))
        banService.removeBan(user.key())
        banService.banChat(chat, description)
        Assertions.assertTrue(banService.getChatBan(context)?.contains(description) ?: false)
        Assertions.assertNull(banService.getUserBan(context))
        Assertions.assertNull(banService.findBanByKey(context.userAndChatKey))
        Assertions.assertNotNull(banService.findBanByKey(chat.key()))
        Assertions.assertNull(banService.findBanByKey(user.key()))
        banService.removeBan(user.key())
    }

    private fun updateFromDeveloper(messageText: String): ExecutorContext {
        val developerUsername = botConfig.developer
        return createSimpleContext(messageText) {
            message.from.userName = developerUsername
            message.chat.apply {
                type = "private"
                userName = developerUsername
            }
        }
    }

    data class BanTestModel(
        val key: String?,
        val easyKey: EasyKey,
    )
}
