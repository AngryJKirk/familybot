package dev.storozhenko.familybot.other

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import dev.storozhenko.familybot.executors.eventbased.keyword.processor.BotMentionKeyWordProcessor
import dev.storozhenko.familybot.infrastructure.createSimpleContext
import dev.storozhenko.familybot.infrastructure.createSimpleMessage
import dev.storozhenko.familybot.infrastructure.createSimpleUser
import dev.storozhenko.familybot.suits.FamilybotApplicationTest
import dev.storozhenko.familybot.telegram.BotConfig

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
        val context = createSimpleContext(phrase)
        context.message.replyToMessage = createSimpleMessage()
        context.message.replyToMessage.from = createSimpleUser(true, botName)
        val canProcess = botMentionKeyWordProcessor.isFuckOff(context)
        Assertions.assertTrue(canProcess, "Should be able to process simple message to bot: $phrase")
    }
}
