package space.yaroslav.familybot.other

import org.junit.Assert
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import space.yaroslav.familybot.executors.eventbased.keyword.FuckOffKeyWordProcessor
import space.yaroslav.familybot.infrastructure.ChatBuilder
import space.yaroslav.familybot.infrastructure.MessageBuilder
import space.yaroslav.familybot.infrastructure.UserBuilder
import space.yaroslav.familybot.infrastructure.randomUUID
import space.yaroslav.familybot.suits.FamilybotApplicationTest
import space.yaroslav.familybot.telegram.BotConfig
import space.yaroslav.familybot.telegram.FamilyBot

class FuckOffTest : FamilybotApplicationTest() {

    @Autowired
    lateinit var fuckOffKeyWordProcessor: FuckOffKeyWordProcessor

    @Autowired
    lateinit var botConfig: BotConfig

    @Test
    fun `should be able to process valid message`() {
        val botName = botConfig.botname ?: throw FamilyBot.InternalException("Wrong test configuration")
        val phraseSet = setOf(
            "ебало    завали",
            "ебало завалил",
            "ебало бля завали",
            "ебало, бля, завали",
            "ебало 123 завали",
            "завали ебало",
            "завали бля ебало",
            "завали, бля, ебало",
            "завали    ебало"
        ).let { set -> set.plus(set.map { randomUUID() + it + randomUUID() }) }

        phraseSet.forEach { phrase ->
            val message = with(MessageBuilder()) {
                text { phrase }
                to { MessageBuilder().from { UserBuilder().toBot(botName) } }
                from { UserBuilder() }
                chat { ChatBuilder() }
            }.build()
            val canProcess = fuckOffKeyWordProcessor.canProcess(message)
            Assert.assertTrue("Should be able to process simple message to bot: $phrase", canProcess)
        }
    }
}
