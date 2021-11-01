package space.yaroslav.familybot.executors

import kotlinx.coroutines.runBlocking
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.executors.command.stats.MeCommandExecutor
import space.yaroslav.familybot.infrastructure.createSimpleCommand
import space.yaroslav.familybot.suits.CommandExecutorTest

class MeExecutorTest : CommandExecutorTest() {

    @Autowired
    lateinit var meCommandExecutor: MeCommandExecutor

    override fun getCommandExecutor(): CommandExecutor = meCommandExecutor

    override fun executeTest() {
        val update = createSimpleCommand(meCommandExecutor.command())
        runBlocking { meCommandExecutor.execute(update).invoke(sender) }
        verify(sender, Mockito.atLeastOnce()).execute(any<SendMessage>())
    }
}
