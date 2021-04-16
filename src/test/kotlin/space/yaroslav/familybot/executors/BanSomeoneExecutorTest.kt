package space.yaroslav.familybot.executors

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Ignore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.executors.pm.BanSomeoneExecutor
import space.yaroslav.familybot.infrastructure.createSimpleUpdate
import space.yaroslav.familybot.infrastructure.randomUUID
import space.yaroslav.familybot.models.Priority
import space.yaroslav.familybot.repos.BanEntity
import space.yaroslav.familybot.repos.BanEntityType
import space.yaroslav.familybot.repos.BanRepository
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
                Arguments.of(BanTestModel(userToBan.name, BanEntity(userToBan.id, BanEntityType.USER))),
                Arguments.of(BanTestModel(userToBan.nickname, BanEntity(userToBan.id, BanEntityType.USER))),
                Arguments.of(BanTestModel(userToBan.id.toString(), BanEntity(userToBan.id, BanEntityType.USER))),
                Arguments.of(BanTestModel(chatToBan.id.toString(), BanEntity(chatToBan.id, BanEntityType.CHAT))),
                Arguments.of(BanTestModel(chatToBan.name, BanEntity(chatToBan.id, BanEntityType.CHAT)))
            )
        }
    }

    @Autowired
    lateinit var banSomeoneExecutor: BanSomeoneExecutor

    @Autowired
    lateinit var botConfig: BotConfig

    @Autowired
    lateinit var banRepository: BanRepository

    override fun priorityTest() {
        val priority = banSomeoneExecutor.priority(Update())
        Assertions.assertEquals(Priority.HIGH, priority)
    }

    override fun canExecuteTest() {
        val validMessage = updateFromDeveloper("BAN1488").message

        val canExecuteValid = banSomeoneExecutor.canExecute(validMessage)

        Assert.assertTrue(canExecuteValid)

        val notValidMessage = updateFromDeveloper(randomUUID()).message

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
        val description = randomUUID()
        val update = updateFromDeveloper("BAN1488|${banModel.key}|$description")
        runBlocking { banSomeoneExecutor.execute(update).invoke(sender) }
        verify(sender).execute(any<SendMessage>())

        val ban = banRepository.getByEntity(banModel.banEntity)
            ?: throw AssertionError("Should be a new ban")

        banRepository.reduceBan(ban)
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
        val banEntity: BanEntity
    )
}
