package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendSticker
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.chatId
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.toChat
import space.yaroslav.familybot.common.extensions.toUser
import space.yaroslav.familybot.models.Marriage
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.repos.MarriagesRepository
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.ProposalTo
import space.yaroslav.familybot.services.settings.UserAndChatEasyKey
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.telegram.BotConfig
import space.yaroslav.familybot.telegram.FamilyBot
import java.io.InputStream
import java.time.Duration

@Component
class MarriageExecutor(
    private val marriagesRepository: MarriagesRepository,
    private val keyValueService: EasyKeyValueService,
    private val dictionary: Dictionary,
    botConfig: BotConfig
) : CommandExecutor(botConfig) {
    private val selfSuckStream: InputStream = this::class.java.classLoader
        .getResourceAsStream("static/selfsuck.webp")
        ?: throw FamilyBot.InternalException("selfsuck.webp is missing")

    override fun command() = Command.MARRY

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val context = dictionary.createContext(update)
        if (!update.message.isReply) {
            return { sender -> sender.send(update, context.get(Phrase.MARRY_RULES)) }
        }
        val chat = update.toChat()
        val proposalTarget = update.message.replyToMessage
        val proposalSource = update.message

        if (proposalTarget.from.id == proposalSource.from.id) {
            return { sender ->
                sender.execute(
                    SendSticker(
                        chat.idString,
                        InputFile(selfSuckStream, "selfsuck")
                    ).apply { replyToMessageId = update.message.messageId }
                )
            }
        }

        if (isMarriedAlready(chat, proposalSource)) {
            return { sender -> sender.send(update, context.get(Phrase.MARRY_SOURCE_IS_MARRIED), replyToUpdate = true) }
        }

        if (isMarriedAlready(chat, proposalTarget)) {
            return { sender -> sender.send(update, context.get(Phrase.MARRY_TARGET_IS_MARRIED), replyToUpdate = true) }
        }
        if (isProposedAlready(proposalSource, proposalTarget)) {
            return { sender -> sender.send(update, context.get(Phrase.MARRY_PROPOSED_AGAIN), replyToUpdate = true) }
        }

        val proposal = keyValueService.get(ProposalTo, proposalSource.key())
        // пропозал делается повторно
        return if (proposal != null && proposal == proposalTarget.from.id) {
            marry(update)
        } else {
            propose(proposalSource, proposalTarget, update)
        }
    }

    private fun isProposedAlready(
        proposalSource: Message,
        proposalTarget: Message,
    ): Boolean {
        return keyValueService.get(ProposalTo, proposalTarget.key()) == proposalSource.from.id
    }

    private fun isMarriedAlready(
        chat: Chat,
        proposalSource: Message
    ) = marriagesRepository.getMarriage(chat.id, proposalSource.from.id) != null

    private fun propose(
        proposalSource: Message,
        proposalTarget: Message,
        update: Update
    ): suspend (AbsSender) -> Unit {
        keyValueService.put(
            ProposalTo,
            key = proposalTarget.key(),
            value = proposalSource.from.id,
            duration = Duration.ofMinutes(10)
        )
        return { sender -> sender.send(update, dictionary.get(Phrase.MARRY_PROPOSED, update), replyMessageId = proposalTarget.messageId) }
    }

    private fun marry(update: Update): suspend (AbsSender) -> Unit {
        val proposalTarget = update.message.replyToMessage.from.toUser(chat = update.toChat())
        val proposalSource = update.toUser()
        val marriage = Marriage(update.chatId(), proposalTarget, proposalSource)
        marriagesRepository.addMarriage(marriage)
        keyValueService.remove(ProposalTo, UserAndChatEasyKey(proposalTarget.id, update.toChat().id))
        return { sender -> sender.send(update, dictionary.get(Phrase.MARRY_CONGRATS, update)) }
    }
}