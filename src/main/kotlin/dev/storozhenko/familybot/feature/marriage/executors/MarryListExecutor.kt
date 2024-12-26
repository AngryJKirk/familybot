package dev.storozhenko.familybot.feature.marriage.executors

import dev.storozhenko.familybot.common.extensions.PluralizedWordsProvider
import dev.storozhenko.familybot.common.extensions.bold
import dev.storozhenko.familybot.common.extensions.pluralize

import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.marriage.model.Marriage
import dev.storozhenko.familybot.feature.marriage.repos.MarriagesRepository
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
class MarryListExecutor(
    private val marriagesRepository: MarriagesRepository,
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
        "♥️",
    )

    override fun command() = Command.MARRY_LIST

    override suspend fun execute(context: ExecutorContext) {
        val marriages = marriagesRepository.getAllMarriages(context.chat.id)
        if (marriages.isEmpty()) {
            context.send(context.phrase(Phrase.MARRY_EMPTY_LIST))
        } else {
            val marriageList = format(marriages, context)
            context.send(marriageList, enableHtml = true)
        }
    }

    private fun format(marriages: List<Marriage>, context: ExecutorContext): String {
        val title = context.phrase(Phrase.MARRY_LIST_TITLE) + "\n"
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

    private fun getEnding(amountOfDays: Long, context: ExecutorContext, marriage: Marriage): String {
        val pluralization = PluralizedWordsProvider(
            one = { context.phrase(Phrase.PLURALIZED_DAY_ONE) },
            few = { context.phrase(Phrase.PLURALIZED_DAY_FEW) },
            many = { context.phrase(Phrase.PLURALIZED_DAY_MANY) },
        )
        val emojiId = (marriage.firstUser.id + marriage.secondUser.id) % loveEmojis.size
        return pluralize(amountOfDays.toInt(), pluralization) + " " + (loveEmojis.getOrNull(emojiId.toInt()) ?: "")
    }
}
