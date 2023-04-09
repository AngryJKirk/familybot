package dev.storozhenko.familybot.feature.pidor.executors

import dev.storozhenko.familybot.common.extensions.PluralizedWordsProvider
import dev.storozhenko.familybot.common.extensions.bold
import dev.storozhenko.familybot.common.extensions.formatTopList
import dev.storozhenko.familybot.common.extensions.key
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.executors.Configurable
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.models.telegram.User
import dev.storozhenko.familybot.core.repos.UserRepository
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.pidor.repos.PidorStrikeStorage
import dev.storozhenko.familybot.feature.settings.models.FunctionId
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class PidorStatsStrikesExecutor(
    private val pidorStrikeStorage: PidorStrikeStorage,
    private val userRepository: UserRepository
) : CommandExecutor(), Configurable {

    override fun getFunctionId(context: ExecutorContext) = FunctionId.PIDOR

    override fun command() = Command.STATS_STRIKES

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val chat = context.chat
        val strikes = pidorStrikeStorage.get(chat.key()).stats.filter { (_, stats) -> stats.maxStrike > 1 }
        val users = userRepository.getUsers(chat).associateBy(User::id)
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
