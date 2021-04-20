package space.yaroslav.familybot.executors.command

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.Pidor
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.bold
import space.yaroslav.familybot.common.utils.getLogger
import space.yaroslav.familybot.common.utils.isToday
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.startOfDay
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.models.Command
import space.yaroslav.familybot.models.FunctionId
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.repos.CommandHistoryRepository
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.services.misc.PidorCompetitionService
import space.yaroslav.familybot.services.misc.PidorStrikesService
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.services.talking.DictionaryContext
import space.yaroslav.familybot.telegram.BotConfig
import java.time.Instant

@Component
class PidorExecutor(
    private val repository: CommonRepository,
    private val pidorCompetitionService: PidorCompetitionService,
    private val dictionary: Dictionary,
    private val commandHistoryRepository: CommandHistoryRepository,
    private val pidorStrikesService: PidorStrikesService,
    config: BotConfig
) : CommandExecutor(config), Configurable {
    override fun getFunctionId(): FunctionId {
        return FunctionId.PIDOR
    }

    private val log = getLogger()

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val chat = update.toChat()
        val context = dictionary.createContext(chat)
        log.info("Getting pidors from chat $chat")
        val users = repository.getUsers(chat, activeOnly = true)
        if (isLimitOfPidorsExceeded(users, chat)) {
            log.info("Pidors are already found")
            val message = getMessageForPidors(chat, context)
            return { it.execute(message) }
        }
        return { sender ->
            log.info("Pidor is not found, initiating search procedure")
            val nextPidor = getNextPidorAsync(users, sender, chat)

            listOf(
                Phrase.PIDOR_SEARCH_START,
                Phrase.PIDOR_SEARCH_MIDDLE,
                Phrase.PIDOR_SEARCH_FINISHER
            )
                .map(context::get)
                .map(String::bold)
                .forEach { phrase ->
                    sender.send(
                        update,
                        phrase,
                        enableHtml = true,
                        shouldTypeBeforeSend = true,
                        typeDelay = 1500L to 1501L
                    )
                }
            val pidor = nextPidor.await()
            sender.send(
                update,
                pidor.getGeneralName(),
                enableHtml = true,
                shouldTypeBeforeSend = true,
                typeDelay = 1500L to 1501L
            )
            pidorStrikesService.calculateStrike(update, pidor).invoke(sender)
            pidorCompetitionService.pidorCompetition(update).invoke(sender)
        }
    }

    private suspend fun getNextPidorAsync(
        users: List<User>,
        sender: AbsSender,
        chat: Chat
    ): Deferred<User> {
        return coroutineScope {
            async {
                users
                    .map { user -> launch { checkIfUserStillThere(user, sender) } }
                    .forEach { job -> job.join() }
                val actualizedUsers = repository.getUsers(chat, activeOnly = true)
                log.info("Users to roll: {}", actualizedUsers)
                val nextPidor = actualizedUsers.random()
                log.info("Pidor is rolled to $nextPidor")
                val newPidor = Pidor(nextPidor, Instant.now())
                repository.addPidor(newPidor)
                return@async nextPidor
            }
        }
    }

    private fun checkIfUserStillThere(
        user: User,
        sender: AbsSender
    ) {
        val userFromChat = getUserFromChat(user, sender)
        if (userFromChat == null) {
            log.warn("Some user {} has left without notification", user)
            repository.changeUserActiveStatusNew(user, false)
        } else {
            repository.addUser(userFromChat)
        }
    }

    override fun command(): Command {
        return Command.PIDOR
    }

    private fun getMessageForPidors(chat: Chat, context: DictionaryContext): SendMessage? {
        val pidorsByChat = repository
            .getPidorsByChat(chat)
            .filter { pidor -> pidor.date.isToday() }
            .distinctBy { pidor -> pidor.user.id }
        return when (pidorsByChat.size) {
            0 -> null
            1 -> SendMessage(
                chat.idString,
                context.get(Phrase.PIROR_DISCOVERED_ONE) + ": " +
                    pidorsByChat.first().user.getGeneralName()
            ).apply { enableHtml(true) }
            else -> SendMessage(
                chat.idString,
                context.get(Phrase.PIROR_DISCOVERED_MANY) + ": " +
                    pidorsByChat.joinToString { it.user.getGeneralName() }
            ).apply { enableHtml(true) }
        }
    }

    private fun isLimitOfPidorsExceeded(
        usersInChat: List<User>,
        chat: Chat
    ): Boolean {
        val limit = if (usersInChat.size >= 50) 2 else 1
        return commandHistoryRepository
            .getAll(chat, Instant.now().startOfDay())
            .filter { todayCommand -> todayCommand.command == command() }
            .size >= limit
    }

    private fun getUserFromChat(user: User, absSender: AbsSender): User? {
        val getChatMemberCall = GetChatMember(user.chat.idString, user.id)
        return runCatching {
            absSender.execute(getChatMemberCall)
                .takeIf { member -> member.status != "left" && member.status != "kicked" }
                ?.user
                ?.toUser(user.chat)
        }.getOrNull()
    }
}
