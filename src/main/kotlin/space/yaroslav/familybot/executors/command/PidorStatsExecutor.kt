package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.PluralizedWordsProvider
import space.yaroslav.familybot.common.utils.bold
import space.yaroslav.familybot.common.utils.formatTopList
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.models.Command
import space.yaroslav.familybot.models.FunctionId
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.services.dictionary.Dictionary
import space.yaroslav.familybot.telegram.BotConfig

@Component
class PidorStatsExecutor(
    private val repository: CommonRepository,
    private val dictionary: Dictionary,
    config: BotConfig
) : CommandExecutor(config), Configurable {

    override fun getFunctionId(): FunctionId {
        return FunctionId.PIDOR
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val chat = update.message.chat.toChat()
        val pidorsByChat = repository.getPidorsByChat(chat)
            .map { it.user }
            .formatTopList(
                PluralizedWordsProvider(
                    one = { dictionary.get(Phrase.PLURALIZED_COUNT_ONE) },
                    few = { dictionary.get(Phrase.PLURALIZED_COUNT_FEW) },
                    many = { dictionary.get(Phrase.PLURALIZED_COUNT_MANY) }
                )
            )
            .take(100)
        val title = "${dictionary.get(Phrase.PIDOR_STAT_ALL_TIME)}:\n".bold()
        return {
            it.send(update, title + pidorsByChat.joinToString("\n"), enableHtml = true)
        }
    }

    override fun command(): Command {
        return Command.STATS_TOTAL
    }
}
