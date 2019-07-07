package space.yaroslav.familybot.route.executors.command

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.Pidor
import space.yaroslav.familybot.common.utils.bold
import space.yaroslav.familybot.common.utils.isToday
import space.yaroslav.familybot.common.utils.randomNotNull
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.services.PidorCompetitionService
import space.yaroslav.familybot.route.services.dictionary.Dictionary
import java.time.Instant

@Component
class PidorExecutor(
    val repository: CommonRepository,
    val pidorCompetitionService: PidorCompetitionService,
    val dictionary: Dictionary
) : CommandExecutor, Configurable {
    override fun getFunctionId(): FunctionId {
        return FunctionId.PIDOR
    }

    private final val log = LoggerFactory.getLogger(PidorExecutor::class.java)

    override fun execute(update: Update): (AbsSender) -> Unit {
        val chat = update.message.chat.toChat()
        log.info("Getting pidors from chat $chat")
        val message = todayPidors(update)

        if (message != null) {
            log.info("Pidors is already founded")
            return { it.execute(message) }
        }

        log.info("Pidor is not found, initiating search procedure")
        val users = repository.getUsers(chat, activeOnly = true)
        log.info("Users to roll: {}", users)
        val nextPidor = users.randomNotNull()
        log.info("Pidor is rolled to $nextPidor")
        val newPidor = Pidor(nextPidor, Instant.now())
        repository.addPidor(newPidor)
        val start = dictionary.get(Phrase.PIDOR_SEARCH_START).bold()
        val middle = dictionary.get(Phrase.PIDOR_SEARCH_MIDDLE).bold()
        val finisher = dictionary.get(Phrase.PIDOR_SEARCH_FINISHER).bold()
        return {
            GlobalScope.launch {
                it.send(update, start, enableHtml = true)
                delay(3000)
                it.send(update, middle, enableHtml = true)
                delay(3000)
                it.send(update, finisher, enableHtml = true)
                delay(3000)
                it.send(update, nextPidor.getGeneralName(mention = true), enableHtml = true)
                pidorCompetitionService.pidorCompetition(update)?.invoke(it)
            }
        }
    }

    override fun command(): Command {
        return Command.PIDOR
    }

    private fun todayPidors(update: Update): SendMessage? {
        val pidorsByChat = repository.getPidorsByChat(update.toChat())
            .filter { it.date.isToday() }
            .distinctBy { it.user.id }

        return when (pidorsByChat.size) {
            0 -> null
            1 -> SendMessage(
                update.message.chatId, dictionary.get(Phrase.PIROR_DISCOVERED_ONE) + ": " +
                    pidorsByChat.first().user.getGeneralName()
            )
            else -> SendMessage(update.message.chatId, dictionary.get(Phrase.PIROR_DISCOVERED_MANY) + ": " +
                pidorsByChat.joinToString { it.user.getGeneralName() })
        }
    }
}
