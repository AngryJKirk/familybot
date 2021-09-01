package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.PluralizedWordsProvider
import space.yaroslav.familybot.common.extensions.bold
import space.yaroslav.familybot.common.extensions.pluralize
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.toChat
import space.yaroslav.familybot.models.Marriage
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.repos.MarriagesRepository
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.services.talking.DictionaryContext
import space.yaroslav.familybot.telegram.BotConfig
import java.time.Duration
import java.time.Instant

@Component
class MarryListExecutor(
    private val marriagesRepository: MarriagesRepository,
    private val dictionary: Dictionary,
    botConfig: BotConfig
) : CommandExecutor(botConfig) {

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
        "♥️",
        "❦",
        "☙",
        "♡",
        "🎔",
        "❧",
        "❥"
    )

    override fun command() = Command.MARRY_LIST

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val context = dictionary.createContext(update)
        val marriages = marriagesRepository.getAllMarriages(update.toChat().id)
        if (marriages.isEmpty()) {
            return { sender -> sender.send(update, context.get(Phrase.MARRY_EMPTY_LIST)) }
        } else {
            val marriageList = format(marriages, context)
            return { sender -> sender.send(update, marriageList, enableHtml = true) }
        }
    }

    private fun format(marriages: List<Marriage>, context: DictionaryContext): String {
        val title = context.get(Phrase.MARRY_LIST_TITLE) + "\n"
        return title + marriages
            .sortedBy(Marriage::startDate)
            .mapIndexed { i, marriage ->
                val index = "${i + 1}.".bold()
                val firstUser = marriage.firstUser.getGeneralName(mention = false).bold()
                val secondUser = marriage.secondUser.getGeneralName(mention = false).bold()
                val daysTogether = Duration.between(marriage.startDate, Instant.now()).toDays()
                val ending = getEnding(daysTogether, context, marriage)
                "$index $firstUser + $secondUser = $daysTogether $ending"
            }
            .joinToString(separator = "\n")
    }

    private fun getEnding(amountOfDays: Long, context: DictionaryContext, marriage: Marriage): String {
        val pluralization = PluralizedWordsProvider(
            one = { context.get(Phrase.PLURALIZED_DAY_ONE) },
            few = { context.get(Phrase.PLURALIZED_DAY_FEW) },
            many = { context.get(Phrase.PLURALIZED_DAY_MANY) }
        )
        val emojiId = (marriage.firstUser.id + marriage.secondUser.id) % loveEmojis.size
        return pluralize(amountOfDays.toInt(), pluralization) + " " + (loveEmojis.getOrNull(emojiId.toInt()) ?: "")
    }
}