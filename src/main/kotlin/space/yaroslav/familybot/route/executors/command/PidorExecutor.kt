package space.yaroslav.familybot.route.executors.command

import java.time.Instant
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.Pidor
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.bold
import space.yaroslav.familybot.common.utils.isToday
import space.yaroslav.familybot.common.utils.randomNotNull
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.services.PidorCompetitionService
import space.yaroslav.familybot.route.services.dictionary.Dictionary
import space.yaroslav.familybot.telegram.BotConfig

@Component
class PidorExecutor(
    private val repository: CommonRepository,
    private val pidorCompetitionService: PidorCompetitionService,
    private val dictionary: Dictionary,
    config: BotConfig
) : CommandExecutor(config), Configurable {
    override fun getFunctionId(): FunctionId {
        return FunctionId.PIDOR
    }

    private final val log = LoggerFactory.getLogger(PidorExecutor::class.java)

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val chat = update.message.chat.toChat()
        log.info("Getting pidors from chat $chat")
        val pidorsByChat = repository.getPidorsByChat(update.toChat())
            .filter { it.date.isToday() }
        val users = repository.getUsers(chat, activeOnly = true)

        if (isLimitOfPidorsExceeded(users, pidorsByChat)) {
            log.info("Pidors are already found")
            val message = getMessageForPidors(update, pidorsByChat)
            return { it.execute(message) }
        }

        return { sender ->
            log.info("Pidor is not found, initiating search procedure")
            val nextPidor = GlobalScope.async {
                users.map { user ->
                    launch {
                        val userFromChat = getUserFromChat(user, sender)
                        if (userFromChat == null) {
                            log.warn("Some user {} had left without notification", user)
                            repository.changeUserActiveStatusNew(user, false) // TODO remove old method
                        } else {
                            repository.addUser(userFromChat)
                        }
                    }
                }
                    .forEach { it.join() }
                val actualizedUsers = repository.getUsers(chat, activeOnly = true)
                log.info("Users to roll: {}", users)
                val nextPidor = actualizedUsers.randomNotNull()
                log.info("Pidor is rolled to $nextPidor")
                val newPidor = Pidor(nextPidor, Instant.now())
                repository.addPidor(newPidor)
                return@async nextPidor
            }

            val start = dictionary.get(Phrase.PIDOR_SEARCH_START).bold()
            val middle = dictionary.get(Phrase.PIDOR_SEARCH_MIDDLE).bold()
            val finisher = dictionary.get(Phrase.PIDOR_SEARCH_FINISHER).bold()

            sender.send(update, start, enableHtml = true)
            delay(3000)
            sender.send(update, middle, enableHtml = true)
            delay(3000)
            sender.send(update, finisher, enableHtml = true)
            delay(3000)
            sender.send(update, nextPidor.await().getGeneralName(), enableHtml = true)
            pidorCompetitionService.pidorCompetition(update)?.invoke(sender)
        }
    }

    override fun command(): Command {
        return Command.PIDOR
    }

    private fun getMessageForPidors(update: Update, pidorsByChat: List<Pidor>): SendMessage? {
        val distinctPidors = pidorsByChat.distinctBy { it.user.id }
        return when (distinctPidors.size) {
            0 -> null
            1 -> SendMessage(
                update.message.chatId, dictionary.get(Phrase.PIROR_DISCOVERED_ONE) + ": " +
                    distinctPidors.first().user.getGeneralName()
            ).enableHtml(true)
            else -> SendMessage(update.message.chatId, dictionary.get(Phrase.PIROR_DISCOVERED_MANY) + ": " +
                distinctPidors.joinToString { it.user.getGeneralName() }).enableHtml(true)
        }
    }

    private fun isLimitOfPidorsExceeded(
        usersInChat: List<User>,
        todayPidors: List<Pidor>
    ): Boolean {
        val limit = if (usersInChat.size >= 100) 2 else 1
        return todayPidors.size >= limit
    }

    private fun getUserFromChat(user: User, absSender: AbsSender): User? {
        val getChatMemberCall = GetChatMember()
            .setChatId(user.chat.id)
            .setUserId(user.id.toInt())
        return runCatching {
            absSender.execute(getChatMemberCall)
                .takeIf { it.status != "left" && it.status != "kicked" }
                ?.user
                ?.toUser(user.chat)
        }.getOrNull()
    }
}
