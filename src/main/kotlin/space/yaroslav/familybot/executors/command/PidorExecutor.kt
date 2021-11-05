package space.yaroslav.familybot.executors.command

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.bold
import space.yaroslav.familybot.common.extensions.isToday
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.toUser
import space.yaroslav.familybot.common.extensions.untilNextDay
import space.yaroslav.familybot.common.extensions.user
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.FunctionId
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.models.telegram.Pidor
import space.yaroslav.familybot.models.telegram.User
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.services.pidor.PidorCompetitionService
import space.yaroslav.familybot.services.pidor.PidorStrikesService
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.PidorTolerance
import java.time.Instant

@Component
class PidorExecutor(
    private val repository: CommonRepository,
    private val pidorCompetitionService: PidorCompetitionService,
    private val pidorStrikesService: PidorStrikesService,
    private val easyKeyValueService: EasyKeyValueService
) : CommandExecutor(), Configurable {
    override fun getFunctionId(executorContext: ExecutorContext): FunctionId {
        return FunctionId.PIDOR
    }

    private val log = getLogger()

    override fun execute(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {
        val chat = executorContext.chat

        log.info("Getting pidors from chat $chat")
        val users = repository.getUsers(chat, activeOnly = true)
        val key = chat.key()
        val pidorToleranceValue = easyKeyValueService.get(PidorTolerance, key)
        if (isLimitOfPidorsExceeded(users, pidorToleranceValue ?: 0)) {
            log.info("Pidors are already found")
            val message = getMessageForPidors(executorContext)
            if (message != null) {
                return { it.execute(message) }
            }
        }
        return { sender ->
            log.info("Pidor is not found, initiating search procedure")
            val nextPidor = getNextPidorAsync(users, sender, chat)

            listOf(
                Phrase.PIDOR_SEARCH_START,
                Phrase.PIDOR_SEARCH_MIDDLE,
                Phrase.PIDOR_SEARCH_FINISHER
            )
                .map(executorContext::phrase)
                .map(String::bold)
                .forEach { phrase ->
                    sender.send(
                        executorContext,
                        phrase,
                        enableHtml = true,
                        shouldTypeBeforeSend = true,
                        typeDelay = 1500 to 1501
                    )
                }
            val pidor = nextPidor.await()
            sender.send(
                executorContext,
                pidor.getGeneralName(),
                enableHtml = true,
                shouldTypeBeforeSend = true,
                typeDelay = 1500 to 1501
            )
            if (pidorToleranceValue == null) {
                easyKeyValueService.put(PidorTolerance, key, 1, untilNextDay())
            } else {
                easyKeyValueService.increment(PidorTolerance, key)
            }
            pidorStrikesService.calculateStrike(executorContext, pidor).invoke(sender)
            pidorCompetitionService.pidorCompetition(executorContext).invoke(sender)
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

    private fun getMessageForPidors(executorContext: ExecutorContext): SendMessage? {
        val chat = executorContext.chat
        val pidorsByChat = repository
            .getPidorsByChat(chat)
            .filter { pidor -> pidor.date.isToday() }
            .distinctBy { (user) -> user.id }
        return when (pidorsByChat.size) {
            0 -> null
            1 -> SendMessage(
                chat.idString,
                executorContext.phrase(Phrase.PIROR_DISCOVERED_ONE) + " " +
                    pidorsByChat.first().user.getGeneralName()
            ).apply { enableHtml(true) }
            else -> SendMessage(
                chat.idString,
                executorContext.phrase(Phrase.PIROR_DISCOVERED_MANY) + " " +
                    pidorsByChat.joinToString { it.user.getGeneralName() }
            ).apply { enableHtml(true) }
        }
    }

    private fun isLimitOfPidorsExceeded(
        usersInChat: List<User>,
        pidorToleranceValue: Long
    ): Boolean {
        val limit = if (usersInChat.size >= 50) 2 else 1
        return pidorToleranceValue >= limit
    }

    private fun getUserFromChat(user: User, absSender: AbsSender): User? {
        val getChatMemberCall = GetChatMember(user.chat.idString, user.id)
        return runCatching {
            absSender.execute(getChatMemberCall)
                .takeIf { member -> member.status != "left" && member.status != "kicked" }
                ?.user()
                ?.toUser(user.chat)
        }.getOrNull()
    }
}
