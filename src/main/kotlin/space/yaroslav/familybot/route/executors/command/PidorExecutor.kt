package space.yaroslav.familybot.route.executors.command

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.Pidor
import space.yaroslav.familybot.common.utils.bold
import space.yaroslav.familybot.common.utils.isToday
import space.yaroslav.familybot.common.utils.random
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.repos.ifaces.PidorDictionaryRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.route.services.PidorCompetitionService
import java.time.Instant

@Component
class PidorExecutor(
    val repository: CommonRepository,
    val dictionaryRepository: PidorDictionaryRepository,
    val pidorCompetitionService: PidorCompetitionService
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
        } else {
            log.info("Pidor is not found, initiating search procedure")
            val users = repository.getUsers(chat, activeOnly = true)
            log.info("Users to roll: {}", users)
            val nextPidor = users.random()!!
            log.info("Pidor is rolled to $nextPidor")
            val newPidor = Pidor(nextPidor, Instant.now())
            repository.addPidor(newPidor)
            val start = dictionaryRepository.getStart().random().bold()
            val middle = dictionaryRepository.getMiddle().random().bold()
            val finisher = dictionaryRepository.getFinish().random().bold()
            return {
                val chatId = update.message.chatId
                it.execute(SendMessage(chatId, start).enableHtml(true))
                Thread.sleep(3000)
                it.execute(SendMessage(chatId, middle).enableHtml(true))
                Thread.sleep(3000)
                it.execute(SendMessage(chatId, finisher).enableHtml(true))
                Thread.sleep(3000)
                it.execute(SendMessage(chatId, nextPidor.getGeneralName(true)))
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
                update.message.chatId, "Сегодняшний пидор уже обнаружен: " +
                    pidorsByChat.first().user.getGeneralName()
            )
            else -> SendMessage(update.message.chatId, "Сегодняшние пидоры уже обнаружены: " +
                pidorsByChat.joinToString { it.user.getGeneralName() })
        }
    }
}
