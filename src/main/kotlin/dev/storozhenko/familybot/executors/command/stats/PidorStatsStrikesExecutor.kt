package dev.storozhenko.familybot.executors.command.stats

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import dev.storozhenko.familybot.common.extensions.PluralizedWordsProvider
import dev.storozhenko.familybot.common.extensions.bold
import dev.storozhenko.familybot.common.extensions.formatTopList
import dev.storozhenko.familybot.common.extensions.key
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.executors.Configurable
import dev.storozhenko.familybot.executors.command.CommandExecutor
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.router.FunctionId
import dev.storozhenko.familybot.models.telegram.Command
import dev.storozhenko.familybot.models.telegram.User
import dev.storozhenko.familybot.repos.CommonRepository
import dev.storozhenko.familybot.services.pidor.PidorStrikeStorage

@Component
class PidorStatsStrikesExecutor(
    private val pidorStrikeStorage: PidorStrikeStorage,
    private val commonRepository: CommonRepository
) : CommandExecutor(), Configurable {

    override fun getFunctionId(context: ExecutorContext) = FunctionId.PIDOR

    override fun command() = Command.STATS_STRIKES

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val chat = context.chat
        val strikes = pidorStrikeStorage.get(chat.key()).stats.filter { (_, stats) -> stats.maxStrike > 1 }
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
