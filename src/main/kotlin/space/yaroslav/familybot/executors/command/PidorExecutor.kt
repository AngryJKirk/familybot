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
import space.yaroslav.familybot.services.settings.PickPidorAbilityCount
import space.yaroslav.familybot.services.settings.PidorTolerance
import space.yaroslav.familybot.telegram.BotConfig
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class PidorExecutor(
    private val repository: CommonRepository,
    private val pidorCompetitionService: PidorCompetitionService,
    private val pidorStrikesService: PidorStrikesService,
    private val easyKeyValueService: EasyKeyValueService,
    private val botConfig: BotConfig
) : CommandExecutor(), Configurable {
    override fun getFunctionId(context: ExecutorContext): FunctionId {
        return FunctionId.PIDOR
    }

    private val log = getLogger()

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val chat = context.chat
        if (context.message.isReply) {
            return pickPidor(context)
        }
        log.info("Getting pidors from chat $chat")
        val users = repository.getUsers(chat, activeOnly = true)
        val key = context.chatKey
        val pidorToleranceValue = easyKeyValueService.get(PidorTolerance, key)
        if (isLimitOfPidorsExceeded(users, pidorToleranceValue ?: 0)) {
            log.info("Pidors are already found")
            val message = getMessageForPidors(context)
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
                .map(context::phrase)
                .map(String::bold)
                .forEach { phrase ->
                    sender.send(
                        context,
                        phrase,
                        enableHtml = true,
                        shouldTypeBeforeSend = true,
                        typeDelay = 1500 to 1501
                    )
                }
            val pidor = nextPidor.await()
            sender.send(
                context,
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
            pidorStrikesService.calculateStrike(context, pidor).invoke(sender)
            pidorCompetitionService.pidorCompetition(context).invoke(sender)
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
                val nextPidor = actualizedUsers.randomOrNull() ?: getFallbackPidor(chat)

                log.info("Pidor is rolled to $nextPidor")
                val newPidor = Pidor(nextPidor, Instant.now())
                repository.addPidor(newPidor)
                return@async nextPidor
            }
        }
    }

    private fun getFallbackPidor(chat: Chat): User {
        log.error("Can't find pidor due to empty user list for $chat, switching to fallback pidor")
        return repository
            .getPidorsByChat(chat, startDate = Instant.now().minus(10, ChronoUnit.DAYS))
            .random()
            .user
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

    private fun getMessageForPidors(context: ExecutorContext): SendMessage? {
        val chat = context.chat
        val pidorsByChat: List<List<Pidor>> = repository
            .getPidorsByChat(chat)
            .filter { pidor -> pidor.date.isToday() }
            .groupBy { (user) -> user.id }
            .map(Map.Entry<Long, List<Pidor>>::value)
        return when (pidorsByChat.size) {
            0 -> null
            1 -> {
                SendMessage(
                    chat.idString,
                    context.phrase(Phrase.PIROR_DISCOVERED_ONE) + " " +
                        formatName(pidorsByChat.first(), context)
                ).apply { enableHtml(true) }
            }
            else -> SendMessage(
                chat.idString,
                context.phrase(Phrase.PIROR_DISCOVERED_MANY) + " " +
                    pidorsByChat.joinToString { formatName(it, context) }
            ).apply { enableHtml(true) }
        }
    }

    private fun formatName(statEntity: List<Pidor>, context: ExecutorContext): String {
        val pidorCount = statEntity.size
        val pidorName = statEntity.first()
        var message = pidorName.user.getGeneralName()
        if (pidorCount > 1) {
            val pidorCountPhrase =
                when (pidorCount) {
                    2 -> Phrase.PIDOR_COUNT_TWICE
                    3 -> Phrase.PIDOR_COUNT_THRICE
                    4 -> Phrase.PIDOR_COUNT_FOUR_TIMES
                    5 -> Phrase.PIDOR_COUNT_FIVE_TIMES
                    else -> Phrase.PIDOR_COUNT_DOHUYA
                }
            message = "$message (${context.phrase(pidorCountPhrase)})"
        }
        return message
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

    private fun pickPidor(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val abilityCount = easyKeyValueService.get(PickPidorAbilityCount, context.userKey)
        if (abilityCount == 0L) {
            return {
                it.send(
                    context,
                    context.phrase(Phrase.PICK_PIDOR_PAYMENT_REQUIRED),
                    shouldTypeBeforeSend = true,
                    replyToUpdate = true
                )
            }
        }
        val replyMessage = context.message.replyToMessage

        if (replyMessage.from.isBot) {
            if (replyMessage.from.userName == botConfig.botName) {
                return {
                    it.send(
                        context,
                        context.phrase(Phrase.PICK_PIDOR_CURRENT_BOT),
                        shouldTypeBeforeSend = true,
                        replyToUpdate = true
                    )
                }
            } else {
                return {
                    it.send(
                        context,
                        context.phrase(Phrase.PICK_PIDOR_ANY_BOT),
                        shouldTypeBeforeSend = true,
                        replyToUpdate = true
                    )
                }
            }
        }
        val pickedUser = replyMessage.from.toUser(context.chat)
        repository.addUser(pickedUser)
        repository.addPidor(Pidor(pickedUser, Instant.now()))
        easyKeyValueService.decrement(PickPidorAbilityCount, context.userKey)
        return {
            it.send(
                context, context.phrase(Phrase.PICK_PIDOR_PICKED).replace("{}", pickedUser.getGeneralName()),
                shouldTypeBeforeSend = true,
                replyMessageId = replyMessage.messageId
            )
            val newAbilityCount = easyKeyValueService.get(
                PickPidorAbilityCount,
                context.userKey
            )
            if (newAbilityCount == 0L) {
                it.send(
                    context, context.phrase(Phrase.PICK_PIDOR_ABILITY_COUNT_LEFT_NONE),
                    shouldTypeBeforeSend = true,
                    replyToUpdate = true,
                    enableHtml = true
                )
            } else {
                it.send(
                    context,
                    context.phrase(Phrase.PICK_PIDOR_ABILITY_COUNT_LEFT).replace("{}", newAbilityCount.toString()),
                    shouldTypeBeforeSend = true,
                    replyToUpdate = true
                )
            }

        }
    }
}