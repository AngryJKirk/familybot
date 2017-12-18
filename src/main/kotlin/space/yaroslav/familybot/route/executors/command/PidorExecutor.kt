package space.yaroslav.familybot.route.executors.command

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.*
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.repos.ifaces.PidorDictionaryRepository
import space.yaroslav.familybot.route.models.Command
import java.time.Instant
import java.util.concurrent.ThreadLocalRandom

@Component
class PidorExecutor(val repository: CommonRepository, val dictionaryRepository: PidorDictionaryRepository) : CommandExecutor() {


    private final val log = LoggerFactory.getLogger(PidorExecutor::class.java)

    override fun execute(update: Update): (AbsSender) -> Unit {
        val chat = update.message.chat.toChat()
        log.info("Getting pidor from chat $chat")
        val pidor = repository
                .getPidorsByChat(chat).find { it.date.isToday() }
        val message = pidor
                ?.let { SendMessage(update.message.chatId, "Сегодняшний пидор уже обнаружен: ${it.user.getGeneralName(true)}") }
        if (message != null) {
            log.info("Pidor is already founded: $pidor")
            return { it.execute(message) }
        } else {
            log.info("Pidor is not found, initiating search procedure")
            val users = repository.getUsers(chat)
            val id = ThreadLocalRandom.current().nextInt(0, users.size)
            val nextPidor = users[id]
            log.info("Pidor is rolled to $nextPidor")
            repository.addPidor(Pidor(nextPidor, Instant.now()))
            val start = dictionaryRepository.getStart().random().bold()
            val middle = dictionaryRepository.getMiddle().random().bold()
            val finisher = dictionaryRepository.getFinish().random().bold()
            return {
                val chatId = update.message.chatId
                it.execute(SendMessage(chatId, start).enableHtml(true))
                Thread.sleep(1000)
                it.execute(SendMessage(chatId, middle).enableHtml(true))
                Thread.sleep(1000)
                it.execute(SendMessage(chatId, finisher).enableHtml(true))
                Thread.sleep(1000)
                it.execute(SendMessage(chatId, nextPidor.getGeneralName(true))) }
        }
    }

    override fun command(): Command {
        return Command.PIDOR
    }
}