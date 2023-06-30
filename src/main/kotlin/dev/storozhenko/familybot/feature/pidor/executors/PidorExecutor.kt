package dev.storozhenko.familybot.feature.pidor.executors

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.bold
import dev.storozhenko.familybot.common.extensions.isToday
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.common.extensions.sendContextFree
import dev.storozhenko.familybot.common.extensions.toUser
import dev.storozhenko.familybot.common.extensions.untilNextDay
import dev.storozhenko.familybot.common.extensions.user
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.executors.Configurable
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.keyvalue.models.ChatEasyKey
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.models.telegram.User
import dev.storozhenko.familybot.core.repos.UserRepository
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.pidor.models.Pidor
import dev.storozhenko.familybot.feature.pidor.repos.PidorRepository
import dev.storozhenko.familybot.feature.pidor.services.PidorCompetitionService
import dev.storozhenko.familybot.feature.pidor.services.PidorStrikesService
import dev.storozhenko.familybot.feature.settings.models.BotOwnerPidorSkip
import dev.storozhenko.familybot.feature.settings.models.FunctionId
import dev.storozhenko.familybot.feature.settings.models.PickPidorAbilityCount
import dev.storozhenko.familybot.feature.settings.models.PidorTolerance
import dev.storozhenko.familybot.feature.talking.services.Dictionary
import dev.storozhenko.familybot.getLogger
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.bots.AbsSender
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class PidorExecutor(
    private val userRepository: UserRepository,
    private val pidorRepository: PidorRepository,
    private val pidorCompetitionService: PidorCompetitionService,
    private val pidorStrikesService: PidorStrikesService,
    private val easyKeyValueService: EasyKeyValueService,
    private val botConfig: BotConfig,
    private val dictionary: Dictionary,
) : CommandExecutor(), Configurable {
    override fun getFunctionId(context: ExecutorContext): FunctionId {
        return FunctionId.PIDOR
    }

    private val log = getLogger()

    override suspend fun execute(context: ExecutorContext) {
        val chat = context.chat
        if (context.message.isReply) {
            return pickPidor(context)
        }
        log.info("Getting pidors from chat $chat")
        val key = context.chatKey
        selectPidor(chat, key).first.invoke(context.sender)
    }

    fun selectPidor(
        chat: Chat,
        key: ChatEasyKey,
        silent: Boolean = false,
    ): Pair<(suspend (AbsSender) -> Unit), Boolean> {
        val users = userRepository.getUsers(chat, activeOnly = true)

        val pidorToleranceValue = easyKeyValueService.get(PidorTolerance, key)
        if (isLimitOfPidorsExceeded(users, pidorToleranceValue ?: 0)) {
            log.info("Pidors are already found")
            if (!silent) {
                val message = getMessageForPidors(chat, key)
                if (message != null) {
                    return Pair({ it.execute(message) }, false)
                }
            } else {
                return Pair({ }, false)
            }
        }
        return Pair({ sender ->
            log.info("Pidor is not found, initiating search procedure")
            val nextPidor = if (easyKeyValueService.get(BotOwnerPidorSkip, key, false)) {
                val filteredUsers = users.filter { botConfig.developerId != it.id.toString() }
                getNextPidorAsync(filteredUsers, sender, chat)
            } else {
                getNextPidorAsync(users, sender, chat)
            }

            listOf(
                Phrase.PIDOR_SEARCH_START,
                Phrase.PIDOR_SEARCH_MIDDLE,
                Phrase.PIDOR_SEARCH_FINISHER,
            )
                .map { phrase -> dictionary.get(phrase, key) }
                .map(String::bold)
                .forEach { phrase ->
                    sender.sendContextFree(
                        chat.idString,
                        phrase,
                        botConfig,
                        enableHtml = true,
                        shouldTypeBeforeSend = true,
                        typeDelay = 1500 to 1501,
                    )
                }
            val pidor = nextPidor.await()
            sender.sendContextFree(
                chat.idString,
                pidor.getGeneralName(),
                botConfig,
                enableHtml = true,
                shouldTypeBeforeSend = true,
                typeDelay = 1500 to 1501,
            )
            if (pidorToleranceValue == null) {
                easyKeyValueService.put(PidorTolerance, key, 1, untilNextDay())
            } else {
                easyKeyValueService.increment(PidorTolerance, key)
            }
            pidorStrikesService.calculateStrike(chat, key, pidor).invoke(sender)
            pidorCompetitionService.pidorCompetition(chat, key).invoke(sender)
        }, true)
    }

    private suspend fun getNextPidorAsync(
        users: List<User>,
        sender: AbsSender,
        chat: Chat,
    ): Deferred<User> {
        return coroutineScope {
            async {
                runCatching {
                    users
                        .map { user -> launch { checkIfUserStillThere(user, sender) } }
                        .forEach { job -> job.join() }
                    val actualizedUsers = userRepository.getUsers(chat, activeOnly = true)
                    log.info("Users to roll: {}", actualizedUsers)
                    val nextPidor = actualizedUsers.randomOrNull() ?: getFallbackPidor(chat)

                    log.info("Pidor is rolled to $nextPidor")
                    val newPidor = Pidor(nextPidor, Instant.now())
                    pidorRepository.addPidor(newPidor)
                    nextPidor
                }
                    .onFailure { e -> log.error("Something bad is happened on rolling, investigate", e) }
                    .getOrNull() ?: getFallbackPidor(chat)
            }
        }
    }

    private fun getFallbackPidor(chat: Chat): User {
        log.error("Can't find pidor due to empty user list for $chat, switching to fallback pidor")
        return pidorRepository
            .getPidorsByChat(chat, startDate = Instant.now().minus(10, ChronoUnit.DAYS))
            .random()
            .user
    }

    private fun checkIfUserStillThere(
        user: User,
        sender: AbsSender,
    ) {
        val userFromChat = getUserFromChat(user, sender)
        if (userFromChat == null) {
            log.warn("Some user {} has left without notification", user)
            userRepository.changeUserActiveStatusNew(user, false)
        } else {
            userRepository.addUser(userFromChat)
        }
    }

    override fun command(): Command {
        return Command.PIDOR
    }

    private fun getMessageForPidors(chat: Chat, key: ChatEasyKey): SendMessage? {
        val pidorsByChat: List<List<Pidor>> = pidorRepository
            .getPidorsByChat(chat)
            .filter { pidor -> pidor.date.isToday() }
            .groupBy { (user) -> user.id }
            .map(Map.Entry<Long, List<Pidor>>::value)
        return when (pidorsByChat.size) {
            0 -> null
            1 -> {
                SendMessage(
                    chat.idString,
                    dictionary.get(Phrase.PIROR_DISCOVERED_ONE, key) + " " +
                        formatName(pidorsByChat.first(), key),
                ).apply { enableHtml(true) }
            }

            else -> SendMessage(
                chat.idString,
                dictionary.get(Phrase.PIROR_DISCOVERED_MANY, key) + " " +
                    pidorsByChat.joinToString { formatName(it, key) },
            ).apply { enableHtml(true) }
        }
    }

    private fun formatName(statEntity: List<Pidor>, key: ChatEasyKey): String {
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
            message = "$message (${dictionary.get(pidorCountPhrase, key)})"
        }
        return message
    }

    private fun isLimitOfPidorsExceeded(
        usersInChat: List<User>,
        pidorToleranceValue: Long,
    ): Boolean {
        val limit = if (usersInChat.size >= 50) 2 else 1
        log.info("Limit of pidors is $limit, tolerance is $pidorToleranceValue")
        return pidorToleranceValue >= limit
    }

    private fun getUserFromChat(user: User, absSender: AbsSender): User? {
        val getChatMemberCall = GetChatMember(user.chat.idString, user.id)
        return runCatching {
            absSender.execute(getChatMemberCall)
                .apply { log.info("Chat member status: $this ") }
                .takeIf { member -> member.status != "left" && member.status != "kicked" }
                ?.user()
                ?.toUser(user.chat)
        }.getOrNull()
    }

    private suspend fun pickPidor(context: ExecutorContext) {
        val abilityCount = easyKeyValueService.get(PickPidorAbilityCount, context.userKey, 0L)
        if (abilityCount <= 0L) {
            context.sender.send(
                context,
                context.phrase(Phrase.PICK_PIDOR_PAYMENT_REQUIRED),
                shouldTypeBeforeSend = true,
                replyToUpdate = true,
            )
            return
        }
        val replyMessage = context.message.replyToMessage

        if (replyMessage.from.isBot) {
            if (replyMessage.from.userName == botConfig.botName) {
                context.sender.send(
                    context,
                    context.phrase(Phrase.PICK_PIDOR_CURRENT_BOT),
                    shouldTypeBeforeSend = true,
                    replyToUpdate = true,
                )
            } else {
                context.sender.send(
                    context,
                    context.phrase(Phrase.PICK_PIDOR_ANY_BOT),
                    shouldTypeBeforeSend = true,
                    replyToUpdate = true,
                )
            }
            return
        }
        val pickedUser = replyMessage.from.toUser(context.chat)
        userRepository.addUser(pickedUser)
        pidorRepository.addPidor(Pidor(pickedUser, Instant.now()))
        easyKeyValueService.decrement(PickPidorAbilityCount, context.userKey)

        context.sender.send(
            context,
            context.phrase(Phrase.PICK_PIDOR_PICKED).replace("{}", pickedUser.getGeneralName()),
            shouldTypeBeforeSend = true,
            replyMessageId = replyMessage.messageId,
        )
        val newAbilityCount = easyKeyValueService.get(
            PickPidorAbilityCount,
            context.userKey,
        )
        if (newAbilityCount == 0L) {
            context.sender.send(
                context,
                context.phrase(Phrase.PICK_PIDOR_ABILITY_COUNT_LEFT_NONE),
                shouldTypeBeforeSend = true,
                replyToUpdate = true,
                enableHtml = true,
            )
        } else {
            context.sender.send(
                context,
                context.phrase(Phrase.PICK_PIDOR_ABILITY_COUNT_LEFT).replace("{}", newAbilityCount.toString()),
                shouldTypeBeforeSend = true,
                replyToUpdate = true,
            )
        }
    }
}
