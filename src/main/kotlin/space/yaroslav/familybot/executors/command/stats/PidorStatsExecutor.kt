package space.yaroslav.familybot.executors.command.stats

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.PluralizedWordsProvider
import space.yaroslav.familybot.common.extensions.bold
import space.yaroslav.familybot.common.extensions.formatTopList
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.toChat
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.FunctionId
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.telegram.BotConfig

@Component
class PidorStatsExecutor(
    private val repository: CommonRepository,
    private val dictionary: Dictionary,
    config: BotConfig
) : CommandExecutor(config), Configurable {

    override fun getFunctionId(update: Update): FunctionId {
        return FunctionId.PIDOR
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val chat = update.toChat()
        val context = dictionary.createContext(chat)
        val pidorsByChat = repository.getPidorsByChat(chat)
            .map { it.user }
            .formatTopList(
                PluralizedWordsProvider(
                    one = { context.get(Phrase.PLURALIZED_COUNT_ONE) },
                    few = { context.get(Phrase.PLURALIZED_COUNT_FEW) },
                    many = { context.get(Phrase.PLURALIZED_COUNT_MANY) }
                )
            )
            .take(100)
        val title = "${context.get(Phrase.PIDOR_STAT_ALL_TIME)}:\n".bold()
        return {
            it.send(update, title + pidorsByChat.joinToString("\n"), enableHtml = true)
        }
    }

    override fun command(): Command {
        return Command.STATS_TOTAL
    }
}
