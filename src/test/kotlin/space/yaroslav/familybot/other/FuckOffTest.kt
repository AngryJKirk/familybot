package space.yaroslav.familybot.other

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import space.yaroslav.familybot.executors.eventbased.keyword.BotMentionKeyWordProcessor
import space.yaroslav.familybot.infrastructure.createSimpleMessage
import space.yaroslav.familybot.infrastructure.createSimpleUpdate
import space.yaroslav.familybot.infrastructure.createSimpleUser
import space.yaroslav.familybot.suits.FamilybotApplicationTest
import space.yaroslav.familybot.telegram.BotConfig

class FuckOffTest : FamilybotApplicationTest() {

    @Autowired
    lateinit var botMentionKeyWordProcessor: BotMentionKeyWordProcessor

    @Autowired
    lateinit var botConfig: BotConfig

    @ParameterizedTest
    @ValueSource(
        strings = [
            "ебало    завали",
            "ебало завалил",
            "ебало бля завали",
            "ебало, бля, завали",
            "ебало 123 завали",
            "завали ебало",
            "завали бля ебало",
            "завали, бля, ебало",
            "пес, завали, бля, ебало",
            "пес, завали, бля, ебало, епта",
            "пес, завали, бля, ебало епта",
            "завали, бля, ебало епта",
            "завали    ебало"
        ]
    )
    fun `should be able to process valid message`(phrase: String) {
        val botName = botConfig.botName
        val update = createSimpleUpdate(phrase)
        update.message.replyToMessage = createSimpleMessage()
        update.message.replyToMessage.from = createSimpleUser(true, botName)
        val canProcess = botMentionKeyWordProcessor.isFuckOff(update)
        Assertions.assertTrue(canProcess, "Should be able to process simple message to bot: $phrase")
    }
}
