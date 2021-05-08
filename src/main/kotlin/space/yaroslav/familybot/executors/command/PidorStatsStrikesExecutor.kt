package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.models.telegram.User
import space.yaroslav.familybot.common.utils.PluralizedWordsProvider
import space.yaroslav.familybot.common.utils.bold
import space.yaroslav.familybot.common.utils.formatTopList
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.services.pidor.PidorStrikeStorage
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.telegram.BotConfig

@Component
class PidorStatsStrikesExecutor(
    private val dictionary: Dictionary,
    private val pidorStrikeStorage: PidorStrikeStorage,
    private val commonRepository: CommonRepository,
    botConfig: BotConfig
) : CommandExecutor(botConfig) {
    override fun command() = Command.STATS_STRIKES

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val context = dictionary.createContext(update)
        val strikes = pidorStrikeStorage.get(update).stats
        val users = commonRepository.getUsers(update.toChat()).associateBy(User::id)
        val stats = strikes
            .map {
                val user = users[it.key]
                if (user != null) {
                    (1..it.value.maxStrike).map { user }
                } else {
                    emptyList()
                }
            }
            .flatten()
            .formatTopList(
                PluralizedWordsProvider(
                    one = { context.get(Phrase.PIDOR_STRIKE_STAT_PLURALIZED_ONE) },
                    few = { context.get(Phrase.PIDOR_STRIKE_STAT_PLURALIZED_FEW) },
                    many = { context.get(Phrase.PIDOR_STRIKE_STAT_PLURALIZED_MANY) }
                )
            )
        val title = "${context.get(Phrase.PIDOR_STRIKE_STAT_TITLE)}:\n".bold()
        if (stats.isNotEmpty()) {
            return { it.send(update, title + stats.joinToString("\n"), enableHtml = true) }
        } else {
            return { it.send(update, title + context.get(Phrase.PIDOR_STRIKE_STAT_NONE), enableHtml = true) }
        }
    }
}
