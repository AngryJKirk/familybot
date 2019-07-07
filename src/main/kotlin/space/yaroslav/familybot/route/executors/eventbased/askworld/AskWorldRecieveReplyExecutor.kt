package space.yaroslav.familybot.route.executors.eventbased.askworld

import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.AskWorldReply
import space.yaroslav.familybot.common.utils.boldNullable
import space.yaroslav.familybot.common.utils.italic
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.AskWorldRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.models.Priority
import space.yaroslav.familybot.route.services.dictionary.Dictionary
import space.yaroslav.familybot.telegram.BotConfig
import space.yaroslav.familybot.telegram.FamilyBot
import java.time.Instant

@Component
class AskWorldRecieveReplyExecutor(
    val askWorldRepository: AskWorldRepository,
    val botConfig: BotConfig,
    val dictionary: Dictionary
) : Executor, Configurable {
    private val log = LoggerFactory.getLogger(AskWorldRecieveReplyExecutor::class.java)
    override fun getFunctionId(): FunctionId {
        return FunctionId.ASK_WORLD
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val reply = update.message.text
        val chat = update.toChat()
        val user = update.toUser()
        val question =
            askWorldRepository.findQuestionByMessageId(update.message.replyToMessage.messageId + chat.id, chat)

        if (askWorldRepository.isReplied(question, chat, user)) {
            return {
                it.execute(
                    SendMessage(
                        chat.id,
                        "Отвечать можно только раз"
                    ).setReplyToMessageId(update.message.messageId)
                )
            }
        }
        val askWorldReply = AskWorldReply(
            null,
            question.id ?: throw FamilyBot.InternalException("Question id is missing, seems like internal logic error"),
            reply,
            user,
            chat,
            Instant.now()
        )

        return {
            runCatching {
                runBlocking {
                    val id = async { askWorldRepository.addReply(askWorldReply) }
                    val message = question.message.takeIf { it.length < 100 } ?: question.message.take(100) + "..."
                    it.execute(
                        SendMessage(
                            question.chat.id,
                            "${dictionary.get(Phrase.ASK_WORLD_REPLY_FROM_CHAT)} ${update.toChat().name.boldNullable()} " +
                                "от ${user.getGeneralName()} на вопрос \"$message\": ${reply.italic()}"
                        ).enableHtml(true)
                    )
                    launch { askWorldRepository.addReplyDeliver(askWorldReply.copy(id = id.await())) }
                    it.send(update, "Принято и отправлено")
                }
            }.onFailure { e ->
                it.send(update, "Принято")
                log.info("Could not send reply instantly", e)
            }
        }
    }

    override fun canExecute(message: Message): Boolean {
        val text = message.replyToMessage
            ?.takeIf { it.from.bot && it.from.userName == botConfig.botname }
            ?.text ?: return false

        val allPrefixes = dictionary.getAll(Phrase.ASK_WORLD_QUESTION_FROM_CHAT)

        return allPrefixes.map { "$it " }.any { text.startsWith(it) }
    }

    override fun priority(update: Update): Priority {
        return Priority.LOW
    }
}
