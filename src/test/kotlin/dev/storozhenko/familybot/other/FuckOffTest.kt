package dev.storozhenko.familybot.other

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.context
import dev.storozhenko.familybot.feature.talking.services.Dictionary
import dev.storozhenko.familybot.feature.talking.services.keyword.processor.BotMentionKeyWordProcessor
import dev.storozhenko.familybot.infrastructure.TestSender
import dev.storozhenko.familybot.infrastructure.createSimpleMessage
import dev.storozhenko.familybot.infrastructure.createSimpleUpdate
import dev.storozhenko.familybot.infrastructure.createSimpleUser
import dev.storozhenko.familybot.suits.FamilybotApplicationTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired

class FuckOffTest : FamilybotApplicationTest() {

    @Autowired
    lateinit var botMentionKeyWordProcessor: BotMentionKeyWordProcessor

    @Autowired
    lateinit var botConfig: BotConfig

    @Autowired
    lateinit var dictionary: Dictionary

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
            "завали    ебало",
        ],
    )
    fun `should be able to process valid message`(phrase: String) {
        val botName = botConfig.botName
        val context = createSimpleUpdate(phrase).context(botConfig, dictionary, TestSender().sender)
        context.message.replyToMessage = createSimpleMessage()
        context.message.replyToMessage.from = createSimpleUser(true, botName)
        val canProcess = botMentionKeyWordProcessor.isFuckOff(context)
        Assertions.assertTrue(canProcess, "Should be able to process simple message to bot: $phrase")
    }
}
