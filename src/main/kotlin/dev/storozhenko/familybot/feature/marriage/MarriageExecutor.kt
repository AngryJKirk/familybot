package dev.storozhenko.familybot.feature.marriage

import dev.storozhenko.familybot.common.extensions.chatId
import dev.storozhenko.familybot.common.extensions.key
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.common.extensions.toChat
import dev.storozhenko.familybot.common.extensions.toUser
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.feature.marriage.model.Marriage
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.feature.marriage.repos.MarriagesRepository
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.keyvalue.models.ProposalTo
import dev.storozhenko.familybot.core.keyvalue.models.UserAndChatEasyKey
import dev.storozhenko.familybot.telegram.FamilyBot
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendSticker
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.bots.AbsSender
import java.io.InputStream
import kotlin.time.Duration.Companion.minutes

@Component
class MarriageExecutor(
    private val marriagesRepository: MarriagesRepository,
    private val keyValueService: EasyKeyValueService
) : CommandExecutor() {
    private val selfSuckStream: InputStream = this::class.java.classLoader
        .getResourceAsStream("static/selfsuck.webp")
        ?: throw FamilyBot.InternalException("selfsuck.webp is missing")

    override fun command() = Command.MARRY

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        if (!context.message.isReply) {
            return { sender -> sender.send(context, context.phrase(Phrase.MARRY_RULES)) }
        }
        val chat = context.chat
        val proposalTarget = context.message.replyToMessage
        val proposalSource = context.message

        if (proposalTarget.from.id == proposalSource.from.id) {
            return { sender ->
                sender.execute(
                    SendSticker(
                        chat.idString,
                        InputFile(selfSuckStream, "selfsuck")
                    ).apply { replyToMessageId = context.message.messageId }
                )
            }
        }
        if (proposalTarget.from.isBot) {
            return { sender ->
                sender.send(
                    context,
                    context.phrase(Phrase.MARRY_PROPOSED_TO_BOT),
                    replyToUpdate = true
                )
            }
        }
        if (isMarriedAlready(chat, proposalSource)) {
            return { sender ->
                sender.send(
                    context,
                    context.phrase(Phrase.MARRY_SOURCE_IS_MARRIED),
                    replyToUpdate = true
                )
            }
        }

        if (isMarriedAlready(chat, proposalTarget)) {
            return { sender ->
                sender.send(
                    context,
                    context.phrase(Phrase.MARRY_TARGET_IS_MARRIED),
                    replyToUpdate = true
                )
            }
        }
        if (isProposedAlready(proposalSource, proposalTarget)) {
            return { sender ->
                sender.send(
                    context,
                    context.phrase(Phrase.MARRY_PROPOSED_AGAIN),
                    replyToUpdate = true
                )
            }
        }

        val proposal = keyValueService.get(ProposalTo, proposalSource.key())
        return if (proposal != null && proposal == proposalTarget.from.id) {
            marry(context)
        } else {
            propose(proposalSource, proposalTarget, context)
        }
    }

    private fun isProposedAlready(
        proposalSource: Message,
        proposalTarget: Message
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
        context: ExecutorContext
    ): suspend (AbsSender) -> Unit {
        keyValueService.put(
            ProposalTo,
            key = proposalTarget.key(),
            value = proposalSource.from.id,
            duration = 10.minutes
        )
        return { sender ->
            sender.send(
                context,
                context.phrase(Phrase.MARRY_PROPOSED),
                replyMessageId = proposalTarget.messageId
            )
        }
    }

    private fun marry(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val update = context.update
        val proposalTarget = context.message.replyToMessage.from.toUser(chat = context.chat)
        val proposalSource = context.user
        val marriage = Marriage(update.chatId(), proposalTarget, proposalSource)
        marriagesRepository.addMarriage(marriage)
        keyValueService.remove(ProposalTo, UserAndChatEasyKey(proposalTarget.id, update.toChat().id))
        return { sender -> sender.send(context, context.phrase(Phrase.MARRY_CONGRATS)) }
    }
}
