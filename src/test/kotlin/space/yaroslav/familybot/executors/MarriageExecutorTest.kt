package space.yaroslav.familybot.executors

import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.executors.command.MarriageExecutor
import space.yaroslav.familybot.infrastructure.createSimpleCommand
import space.yaroslav.familybot.infrastructure.createSimpleMessage
import space.yaroslav.familybot.infrastructure.createSimpleUser
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.repos.MarriagesRepository
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.ProposalTo
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.suits.CommandExecutorTest
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
        val updateNoReply = createSimpleCommand(marriageExecutor.command())
        runBlocking { marriageExecutor.execute(updateNoReply).invoke(sender) }
        argumentCaptor<SendMessage> {
            verify(sender, times(1)).execute(capture())
            assertContains(phrases(Phrase.MARRY_RULES), firstValue.text)
        }

        val firstUser = createSimpleUser().apply { id = 1 }
        val secondUser = createSimpleUser().apply { id = 2 }
        val updateProposal = createSimpleCommand(marriageExecutor.command())
        updateProposal.apply {
            message.from = firstUser
            message.replyToMessage = createSimpleMessage(chat = updateProposal.message.chat)
                .apply {
                    from = secondUser
                }
        }
        runBlocking { marriageExecutor.execute(updateProposal).invoke(sender) }
        argumentCaptor<SendMessage> {
            verify(sender, times(2)).execute(capture())
            val proposalTo = keyValueService.get(ProposalTo, updateProposal.message.replyToMessage.key())
            assertEquals(proposalTo, firstUser.id)
            assertContains(phrases(Phrase.MARRY_PROPOSED), secondValue.text)
        }

        val updateProposalReply = createSimpleCommand(marriageExecutor.command())
        updateProposalReply.apply {
            message.chat = updateProposal.message.chat
            message.from = secondUser
            message.replyToMessage = createSimpleMessage(chat = updateProposal.message.chat)
                .apply {
                    from = firstUser
                }
        }
        runBlocking { marriageExecutor.execute(updateProposalReply).invoke(sender) }
        argumentCaptor<SendMessage> {
            verify(sender, times(3)).execute(capture())
            assertContains(phrases(Phrase.MARRY_CONGRATS), thirdValue.text)
            assertNotNull(marriagesRepository.getMarriage(chatId = updateProposal.message.chatId, firstUser.id))
            assertNotNull(marriagesRepository.getMarriage(chatId = updateProposal.message.chatId, secondUser.id))
        }
    }

    private fun phrases(phrase: Phrase) = dictionary.getAll(phrase)
}
