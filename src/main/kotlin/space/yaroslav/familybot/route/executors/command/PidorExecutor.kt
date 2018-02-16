package space.yaroslav.familybot.route.executors.command

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.*
import space.yaroslav.familybot.common.utils.bold
import space.yaroslav.familybot.common.utils.isToday
import space.yaroslav.familybot.common.utils.random
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.repos.ifaces.PidorDictionaryRepository
import space.yaroslav.familybot.route.models.Command
import java.time.Instant
import java.time.LocalDateTime

@Component
class PidorExecutor(val repository: CommonRepository, val dictionaryRepository: PidorDictionaryRepository) : CommandExecutor() {


    private final val log = LoggerFactory.getLogger(PidorExecutor::class.java)

    override fun execute(update: Update): (AbsSender) -> Unit {
        val chat = update.message.chat.toChat()
        log.info("Getting pidor from chat $chat")
        val pidorsByChat = repository.getPidorsByChat(chat)
        val pidor = pidorsByChat.find { it.date.isToday() }
        val message = pidor
                ?.let { SendMessage(update.message.chatId, "Сегодняшний пидор уже обнаружен: ${it.user.getGeneralName(true)}") }
        if (message != null) {
            log.info("Pidor is already founded: $pidor")
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
                if (isEndOfMonth()) {
                    val competitors = detectPidorCompetition(pidorsByChat, newPidor)
                    if(competitors != null){
                        it.execute(SendMessage(chatId, "Так-так-так, у нас тут гонка заднеприводных".bold()))
                        val oneMorePidor = competitors.random()!!
                        repository.addPidor(Pidor(oneMorePidor, Instant.now()))
                        Thread.sleep(1000)
                        it.execute(SendMessage(chatId, "Еще один сегодняшний пидор это ".bold() + "${oneMorePidor.nickname}"))
                    }
                }
            }
        }
    }

    override fun command(): Command {
        return Command.PIDOR
    }

    private fun isEndOfMonth(): Boolean {
        val time = LocalDateTime.now()
        return time.month.length(time.year % 4 == 0) == time.dayOfMonth
    }

    private fun detectPidorCompetition(pidors: List<Pidor>, currentPidor: Pidor): Set<User>? {
        val pidorsByUser = pidors.plus(currentPidor).groupBy { it.user }
        val maxCount = pidorsByUser.mapValues { it.value.size }.maxBy { it.value }!!.value
        val competitors = pidorsByUser.filterValues { it.size == maxCount }.keys
        return if (competitors.size > 1) {
            competitors
        } else {
            null
        }
    }

}