package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.bold
import space.yaroslav.familybot.common.utils.italic
import space.yaroslav.familybot.common.utils.pluralize
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.services.dictionary.Dictionary
import space.yaroslav.familybot.telegram.BotConfig

@Component
class PidorStatsExecutor(
    val repository: CommonRepository,
    val dictionary: Dictionary,
    config: BotConfig
) : CommandExecutor(config), Configurable {

    override fun getFunctionId(): FunctionId {
        return FunctionId.PIDOR
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val chat = update.message.chat.toChat()
        val pidorsByChat = repository.getPidorsByChat(chat)
            .groupBy { it.user }
            .map { it.key to it.value.size }
            .sortedByDescending { it.second }
            .mapIndexed { index, pair -> format(index, pair) }
        val title = "${dictionary.get(Phrase.PIDOR_STAT_ALL_TIME)}:\n".bold()
        return {
            it.send(update, title + pidorsByChat.joinToString("\n"), enableHtml = true)
        }
    }

    override fun command(): Command {
        return Command.STATS_TOTAL
    }

    private fun format(index: Int, pidorStats: Pair<User, Int>): String {
        val generalName = pidorStats.first.name ?: pidorStats.first.nickname
        val i = "${index + 1}.".bold()
        val plurWord = pluralize(
            pidorStats.second, dictionary.get(Phrase.PLURALIZED_COUNT_ONE),
            dictionary.get(Phrase.PLURALIZED_COUNT_FEW),
            dictionary.get(Phrase.PLURALIZED_COUNT_MANY)
        )
        val stat = "${pidorStats.second} $plurWord".italic()
        return "$i $generalName — $stat"
    }
}
