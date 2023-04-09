package dev.storozhenko.familybot.executors

import dev.storozhenko.familybot.common.extensions.key
import dev.storozhenko.familybot.feature.marriage.MarriageExecutor
import dev.storozhenko.familybot.infrastructure.createContext
import dev.storozhenko.familybot.infrastructure.createSimpleCommand
import dev.storozhenko.familybot.infrastructure.createSimpleCommandContext
import dev.storozhenko.familybot.infrastructure.createSimpleMessage
import dev.storozhenko.familybot.infrastructure.createSimpleUser
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.feature.marriage.repos.MarriagesRepository
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.keyvalue.models.ProposalTo
import dev.storozhenko.familybot.feature.talking.services.Dictionary
import dev.storozhenko.familybot.suits.CommandExecutorTest
import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MarriageExecutorTest : CommandExecutorTest() {

    @Autowired
    private lateinit var marriageExecutor: MarriageExecutor

    @Autowired
    private lateinit var keyValueService: EasyKeyValueService

    @Autowired
    private lateinit var marriagesRepository: MarriagesRepository

    @Autowired
    private lateinit var dictionary: Dictionary

    override fun getCommandExecutor() = marriageExecutor

    override fun executeTest() {
        val contextNoReply = createSimpleCommandContext(marriageExecutor.command())
        runBlocking { marriageExecutor.execute(contextNoReply).invoke(sender) }
        argumentCaptor<SendMessage> {
            verify(sender, times(1)).execute(capture())
            assertContains(phrases(Phrase.MARRY_RULES), firstValue.text)
        }

        val firstUser = createSimpleUser().apply { id = 1 }
        val secondUser = createSimpleUser().apply { id = 2 }
        val proposalContext = createSimpleCommand(marriageExecutor.command())
        proposalContext.apply {
            message.from = firstUser
            message.replyToMessage = createSimpleMessage(chat = proposalContext.message.chat)
                .apply {
                    from = secondUser
                }
        }
        runBlocking { marriageExecutor.execute(proposalContext.createContext()).invoke(sender) }
        argumentCaptor<SendMessage> {
            verify(sender, times(2)).execute(capture())
            val proposalTo = keyValueService.get(ProposalTo, proposalContext.message.replyToMessage.key())
            assertEquals(proposalTo, firstUser.id)
            assertContains(phrases(Phrase.MARRY_PROPOSED), secondValue.text)
        }

        val updateProposalReply = createSimpleCommand(marriageExecutor.command())
        updateProposalReply.apply {
            message.chat = proposalContext.message.chat
            message.from = secondUser
            message.replyToMessage = createSimpleMessage(chat = proposalContext.message.chat)
                .apply {
                    from = firstUser
                }
        }
        runBlocking { marriageExecutor.execute(updateProposalReply.createContext()).invoke(sender) }
        argumentCaptor<SendMessage> {
            verify(sender, times(3)).execute(capture())
            assertContains(phrases(Phrase.MARRY_CONGRATS), thirdValue.text)
            assertNotNull(marriagesRepository.getMarriage(chatId = proposalContext.message.chatId, firstUser.id))
            assertNotNull(marriagesRepository.getMarriage(chatId = proposalContext.message.chatId, secondUser.id))
        }
    }

    private fun phrases(phrase: Phrase) = dictionary.getAll(phrase)
}
