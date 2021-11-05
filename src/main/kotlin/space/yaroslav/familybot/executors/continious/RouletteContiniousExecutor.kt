package space.yaroslav.familybot.executors.continious

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.randomInt
import space.yaroslav.familybot.executors.command.nonpublic.ROULETTE_MESSAGE
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.models.telegram.Pidor
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.services.pidor.PidorCompetitionService
import space.yaroslav.familybot.telegram.BotConfig
import java.time.Instant

@Component
@Deprecated(message = "Replaced with BetContinious")
class RouletteContiniousExecutor(
    private val botConfig: BotConfig,
    private val pidorRepository: CommonRepository,
    private val pidorCompetitionService: PidorCompetitionService
) : ContiniousConversationExecutor(botConfig) {

    private val log = LoggerFactory.getLogger(RouletteContiniousExecutor::class.java)

    override fun getDialogMessage(executorContext: ExecutorContext): String {
        return ROULETTE_MESSAGE
    }

    override fun command(): Command {
        return Command.ROULETTE
    }

    override fun canExecute(executorContext: ExecutorContext): Boolean {
        val message = executorContext.message
        return message.isReply &&
            message.replyToMessage.from.userName == botConfig.botName &&
            (message.replyToMessage.text ?: "") == getDialogMessage(executorContext)
    }

    override fun execute(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {
        val user = executorContext.user
        val chatId = executorContext.chat.idString

        val number = executorContext.message.text.split(" ")[0].toIntOrNull()
        if (number !in 1..6) {
            return {
                it.execute(SendMessage(chatId, "Мушку спили и в следующий раз играй по правилам"))
                coroutineScope {
                    launch {
                        pidorRepository.addPidor(
                            Pidor(
                                user,
                                Instant.now()
                            )
                        )
                    }
                }
                delay(1000)
                it.execute(SendMessage(chatId, "В наказание твое пидорское очко уходит к остальным"))
            }
        }
        val rouletteNumber = randomInt(1, 7)
        log.info("Roulette win number is $rouletteNumber and guessed number is $number")
        return {
            if (rouletteNumber == number) {
                it.execute(SendMessage(chatId, "Ты ходишь по охуенно тонкому льду"))
                coroutineScope { launch { repeat(5) { pidorRepository.removePidorRecord(user) } } }
                delay(2000)
                it.execute(SendMessage(chatId, "Но он пока не треснул. Свое пидорское очко можешь забрать. "))
            } else {
                it.execute(SendMessage(chatId, "Ты ходишь по охуенно тонкому льду"))
                coroutineScope {
                    launch {
                        repeat(3) {
                            pidorRepository.addPidor(
                                Pidor(
                                    user,
                                    Instant.now()
                                )
                            )
                        }
                    }
                }
                delay(2000)
                it.execute(
                    SendMessage(
                        chatId,
                        "Сорян, но ты проиграл. Твое пидорское очко уходит в зрительный зал трижды. Правильный ответ был $rouletteNumber."
                    )
                )
            }
            delay(2000)
            pidorCompetitionService.pidorCompetition(executorContext).invoke(it)
        }
    }
}
