package dev.storozhenko.familybot.executors

import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.feature.stats.MeCommandExecutor
import dev.storozhenko.familybot.infrastructure.createSimpleCommandContext
import dev.storozhenko.familybot.suits.CommandExecutorTest
import kotlinx.coroutines.runBlocking
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

class MeExecutorTest : CommandExecutorTest() {

    @Autowired
    lateinit var meCommandExecutor: MeCommandExecutor

    override fun getCommandExecutor(): CommandExecutor = meCommandExecutor

    override fun executeTest() {
        val update = createSimpleCommandContext(meCommandExecutor.command())
        runBlocking { meCommandExecutor.execute(update).invoke(sender) }
        verify(sender, Mockito.atLeastOnce()).execute(any<SendMessage>())
    }
}
