package space.yaroslav.familybot.executors.command

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
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
import space.yaroslav.familybot.repos.ifaces.CommandHistoryRepository
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.services.PidorCompetitionService
import space.yaroslav.familybot.services.dictionary.Dictionary
import space.yaroslav.familybot.telegram.BotConfig
import java.time.Instant

@Component
class PidorExecutor(
    private val repository: CommonRepository,
    private val pidorCompetitionService: PidorCompetitionService,
    private val dictionary: Dictionary,
    private val commandHistoryRepository: CommandHistoryRepository,
    config: BotConfig
) : CommandExecutor(config), Configurable {
    override fun getFunctionId(): FunctionId {
        return FunctionId.PIDOR
    }

    private val log = getLogger()

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val chat = update.message.chat.toChat()
        log.info("Getting pidors from chat $chat")
        val users = repository.getUsers(chat, activeOnly = true)
        if (isLimitOfPidorsExceeded(users, chat)) {
            log.info("Pidors are already found")
            val message = getMessageForPidors(chat)
            return { it.execute(message) }
        }
        return { sender ->
            log.info("Pidor is not found, initiating search procedure")
            val nextPidor = GlobalScope.async {
                users.map { user ->
                    launch {
                        val userFromChat = getUserFromChat(user, sender)
                        if (userFromChat == null) {
                            log.warn("Some user {} left without notification", user)
                            repository.changeUserActiveStatusNew(user, false) // TODO remove old method
                        } else {
                            repository.addUser(userFromChat)
                        }
                    }
                }
                    .forEach { it.join() }
                val actualizedUsers = repository.getUsers(chat, activeOnly = true)
                log.info("Users to roll: {}", users)
                val nextPidor = actualizedUsers.random()
                log.info("Pidor is rolled to $nextPidor")
                val newPidor = Pidor(nextPidor, Instant.now())
                repository.addPidor(newPidor)
                return@async nextPidor
            }

            val start = dictionary.get(Phrase.PIDOR_SEARCH_START).bold()
            val middle = dictionary.get(Phrase.PIDOR_SEARCH_MIDDLE).bold()
            val finisher = dictionary.get(Phrase.PIDOR_SEARCH_FINISHER).bold()

            sender.send(update, start, enableHtml = true, shouldTypeBeforeSend = true, typeDelay = 1500L to 1501L)
            sender.send(update, middle, enableHtml = true, shouldTypeBeforeSend = true, typeDelay = 1500L to 1501L)
            sender.send(update, finisher, enableHtml = true, shouldTypeBeforeSend = true, typeDelay = 1500L to 1501L)
            sender.send(
                update,
                nextPidor.await().getGeneralName(),
                enableHtml = true,
                shouldTypeBeforeSend = true,
                typeDelay = 1500L to 1501L
            )
            pidorCompetitionService.pidorCompetition(update)?.invoke(sender)
        }
    }

    override fun command(): Command {
        return Command.PIDOR
    }

    private fun getMessageForPidors(chat: Chat): SendMessage? {
        val pidorsByChat = repository
            .getPidorsByChat(chat)
            .filter { it.date.isToday() }
            .distinctBy { it.user.id }
        return when (pidorsByChat.size) {
            0 -> null
            1 -> SendMessage(
                chat.idString,
                dictionary.get(Phrase.PIROR_DISCOVERED_ONE) + ": " +
                    pidorsByChat.first().user.getGeneralName()
            ).apply { enableHtml(true) }
            else -> SendMessage(
                chat.idString,
                dictionary.get(Phrase.PIROR_DISCOVERED_MANY) + ": " +
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
        val getChatMemberCall = GetChatMember(user.chat.idString, user.id.toInt())
        return runCatching {
            absSender.execute(getChatMemberCall)
                .takeIf { it.status != "left" && it.status != "kicked" }
                ?.user
                ?.toUser(user.chat)
        }.getOrNull()
    }
}
