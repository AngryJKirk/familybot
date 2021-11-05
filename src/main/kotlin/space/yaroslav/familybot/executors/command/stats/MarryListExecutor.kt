package space.yaroslav.familybot.executors.command.stats

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.PluralizedWordsProvider
import space.yaroslav.familybot.common.extensions.bold
import space.yaroslav.familybot.common.extensions.pluralize
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.models.Marriage
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.repos.MarriagesRepository
import java.time.Duration
import java.time.Instant

@Component
class MarryListExecutor(
    private val marriagesRepository: MarriagesRepository
) : CommandExecutor() {

    private val loveEmojis = listOf(
        "🥰",
        "😍",
        "😘",
        "😻",
        "💌",
        "💘",
        "💝",
        "💖",
        "💗",
        "💓",
        "💞",
        "💕",
        "💟",
        "❣️",
        "💔",
        "❤️‍🔥",
        "❤️‍🩹",
        "❤️",
        "🧡",
        "💛",
        "💚",
        "💙",
        "💜",
        "🤎",
        "🖤",
        "🤍",
        "🫀",
        "💏",
        "👩‍❤️‍💋‍👨",
        "👨‍❤️‍💋‍👨",
        "👩‍❤️‍💋‍👩",
        "💑",
        "👩‍❤️‍👨",
        "👨‍❤️‍👨",
        "👩‍❤️‍👩",
        "🏩",
        "💒",
        "♥️"
    )

    override fun command() = Command.MARRY_LIST

    override fun execute(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {
        
        val marriages = marriagesRepository.getAllMarriages(executorContext.chat.id)
        if (marriages.isEmpty()) {
            return { sender -> sender.send(executorContext, executorContext.phrase(Phrase.MARRY_EMPTY_LIST)) }
        } else {
            val marriageList = format(marriages, executorContext)
            return { sender -> sender.send(executorContext, marriageList, enableHtml = true) }
        }
    }

    private fun format(marriages: List<Marriage>, executorContext: ExecutorContext): String {
        val title = executorContext.phrase(Phrase.MARRY_LIST_TITLE) + "\n"
        return title + marriages
            .sortedBy(Marriage::startDate)
            .mapIndexed { i, marriage ->
                val index = "${i + 1}.".bold()
                val firstUser = marriage.firstUser.getGeneralName(mention = false).bold()
                val secondUser = marriage.secondUser.getGeneralName(mention = false).bold()
                val daysTogether = Duration.between(marriage.startDate, Instant.now()).toDays()
                val ending = getEnding(daysTogether, executorContext, marriage)
                "$index $firstUser + $secondUser = $daysTogether $ending"
            }
            .joinToString(separator = "\n")
    }

    private fun getEnding(amountOfDays: Long, executorContext: ExecutorContext, marriage: Marriage): String {
        val pluralization = PluralizedWordsProvider(
            one = { executorContext.phrase(Phrase.PLURALIZED_DAY_ONE) },
            few = { executorContext.phrase(Phrase.PLURALIZED_DAY_FEW) },
            many = { executorContext.phrase(Phrase.PLURALIZED_DAY_MANY) }
        )
        val emojiId = (marriage.firstUser.id + marriage.secondUser.id) % loveEmojis.size
        return pluralize(amountOfDays.toInt(), pluralization) + " " + (loveEmojis.getOrNull(emojiId.toInt()) ?: "")
    }
}
