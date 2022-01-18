package space.yaroslav.familybot.executors.command.stats

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.PluralizedWordsProvider
import space.yaroslav.familybot.common.extensions.bold
import space.yaroslav.familybot.common.extensions.formatTopList
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.FunctionId
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.models.telegram.User
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.services.pidor.PidorStrikeStorage

@Component
class PidorStatsStrikesExecutor(
    private val pidorStrikeStorage: PidorStrikeStorage,
    private val commonRepository: CommonRepository
) : CommandExecutor(), Configurable {

    override fun getFunctionId(context: ExecutorContext) = FunctionId.PIDOR

    override fun command() = Command.STATS_STRIKES

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val chat = context.chat
        val strikes = pidorStrikeStorage.get(context).stats.filter { (_, stats) -> stats.maxStrike > 1 }
        val users = commonRepository.getUsers(chat).associateBy(User::id)
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
                    one = { context.phrase(Phrase.PIDOR_STRIKE_STAT_PLURALIZED_ONE) },
                    few = { context.phrase(Phrase.PIDOR_STRIKE_STAT_PLURALIZED_FEW) },
                    many = { context.phrase(Phrase.PIDOR_STRIKE_STAT_PLURALIZED_MANY) }
                )
            )
        val title = "${context.phrase(Phrase.PIDOR_STRIKE_STAT_TITLE)}:\n".bold()
        if (stats.isNotEmpty()) {
            return { it.send(context, title + stats.joinToString("\n"), enableHtml = true) }
        } else {
            return {
                it.send(
                    context,
                    title + context.phrase(Phrase.PIDOR_STRIKE_STAT_NONE),
                    enableHtml = true
                )
            }
        }
    }
}
