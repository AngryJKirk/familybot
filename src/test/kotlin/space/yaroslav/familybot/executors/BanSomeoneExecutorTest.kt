package space.yaroslav.familybot.executors

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.springframework.beans.factory.annotation.Autowired
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.executors.pm.BanSomeoneExecutor
import space.yaroslav.familybot.infrastructure.ChatBuilder
import space.yaroslav.familybot.infrastructure.MessageBuilder
import space.yaroslav.familybot.infrastructure.UpdateBuilder
import space.yaroslav.familybot.infrastructure.UserBuilder
import space.yaroslav.familybot.models.Priority
import space.yaroslav.familybot.repos.ifaces.BanEntity
import space.yaroslav.familybot.repos.ifaces.BanEntityType
import space.yaroslav.familybot.repos.ifaces.BanRepository
import space.yaroslav.familybot.suits.ExecutorTest
import space.yaroslav.familybot.telegram.BotConfig
import space.yaroslav.familybot.telegram.FamilyBot
import java.util.UUID

class BanSomeoneExecutorTest : ExecutorTest() {

    @Autowired
    lateinit var banSomeoneExecutor: BanSomeoneExecutor

    @Autowired
    lateinit var botConfig: BotConfig

    @Autowired
    lateinit var banRepository: BanRepository

    override fun priotityTest() {
        val priority = banSomeoneExecutor.priority(Update())
        Assert.assertEquals(Priority.HIGH, priority)
    }

    override fun canExecuteTest() {
        val validMessage = updateFromDeveloper("BAN1488").message

        val canExecuteValid = banSomeoneExecutor.canExecute(validMessage)

        Assert.assertTrue(canExecuteValid)

        val notValidMessage = updateFromDeveloper(UUID.randomUUID().toString()).message

        val canExecuteNotValid = banSomeoneExecutor.canExecute(notValidMessage)

        Assert.assertFalse(canExecuteNotValid)
    }

    override fun executeTest() {
        val chatToBan = ChatBuilder().build().toChat()
        val userToBan = UserBuilder().build().toUser(chatToBan)
        banTest { BanTestModel(userToBan.name, BanEntity(userToBan.id, BanEntityType.USER)) }
        banTest { BanTestModel(userToBan.nickname, BanEntity(userToBan.id, BanEntityType.USER)) }
        banTest { BanTestModel(userToBan.id.toString(), BanEntity(userToBan.id, BanEntityType.USER)) }
        banTest { BanTestModel(chatToBan.id.toString(), BanEntity(chatToBan.id, BanEntityType.CHAT)) }
        banTest { BanTestModel(chatToBan.name, BanEntity(chatToBan.id, BanEntityType.CHAT)) }
    }

    private fun updateFromDeveloper(messageText: String): Update {
        val developerUsername =
            botConfig.developer ?: throw FamilyBot.InternalException("Someone wrong with tests setup")
        return UpdateBuilder()
            .message {
                MessageBuilder()
                    .chat { ChatBuilder().becomeUser(developerUsername) }
                    .text { messageText }
                    .from { UserBuilder().username { developerUsername } }
            }.build()
    }

    private fun banTest(model: () -> BanTestModel) {
        val description = UUID.randomUUID()
        val banModel = model()
        val update = updateFromDeveloper("BAN1488|${banModel.key}|$description")
        runBlocking { banSomeoneExecutor.execute(update).invoke(testSender) }
        Assert.assertEquals(1, testSender.actions.size)

        val ban = banRepository.getByEntity(banModel.banEntity) ?: throw AssertionError()

        cleanSender()
        banRepository.reduceBan(ban)
    }

    data class BanTestModel(
        val key: String?,
        val banEntity: BanEntity
    )
}
