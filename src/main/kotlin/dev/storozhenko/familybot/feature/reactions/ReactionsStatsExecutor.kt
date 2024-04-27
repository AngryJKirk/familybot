package dev.storozhenko.familybot.feature.reactions

import dev.storozhenko.familybot.common.extensions.PluralizedWordsProvider
import dev.storozhenko.familybot.common.extensions.bold
import dev.storozhenko.familybot.common.extensions.link
import dev.storozhenko.familybot.common.extensions.pluralize
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.models.telegram.User
import dev.storozhenko.familybot.core.repos.ReactionRepository
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.telegram.FamilyBot
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class ReactionsStatsExecutor(private val reactionRepository: ReactionRepository) : CommandExecutor() {
    override fun command() = Command.REACTION_STATS

    private val pluralizedReactions = PluralizedWordsProvider({ "реакция" }, { "реакции" }, { "реакций" })

    override suspend fun execute(context: ExecutorContext) {
        val message = context.message.text.split(" ").getOrElse(1) { "неделя" }
        val after = when (message) {
            "сегодня" -> Instant.now().minus(24, ChronoUnit.HOURS)
            "неделя" -> Instant.now().minus(7, ChronoUnit.DAYS)
            "месяц" -> Instant.now().minus(30, ChronoUnit.DAYS)
            else -> Instant.now().minus(24, ChronoUnit.HOURS)
        }
        val reactions = reactionRepository.get(context.chat, after)


        context.sender.send(context, calculateReactionStats(reactions), enableHtml = true)
    }


    fun calculateReactionStats(reactionsData: List<ReactionRepository.Reaction>): String {
        val topUsersByReaction = mutableMapOf<User, Int>()
        val totalUserReactions = mutableMapOf<User, List<String>>()

        for (reaction in reactionsData) {
            val user = reaction.to
            val reactions = reaction.reactions

            topUsersByReaction.merge(user, reactions.size, Int::plus)
            totalUserReactions.merge(user, reactions) { l1, l2 -> l1 + l2 }
        }

        return topUsersByReaction
            .entries
            .sortedByDescending { (_, count) -> count }
            .joinToString("\n") { (user, reactionCounter) ->
                formatMessage(
                    user,
                    reactionCounter,
                    totalUserReactions[user] ?: throw FamilyBot.InternalException("some fuck up?")
                )
            }


    }

    private fun formatMessage(user: User, reactionCounter: Int, totalUserReactions: List<String>): String {
        val name = user.getGeneralName(mention = false).bold()
        val plured = pluralize(reactionCounter, pluralizedReactions)
        val countByReactions = totalUserReactions
            .asSequence()
            .map { if (it.length > 30) "другие" else it }
            .groupBy { it }
            .map { (reaction, count) -> reaction to count.size }
            .sortedByDescending { (_, count) -> count }
            .joinToString(" ") { (reaction, count) -> "$reaction $count" }
        return "$name ($reactionCounter $plured):\n$countByReactions"

    }

    private fun messageLink(reaction: ReactionRepository.Reaction): String {
        return "от ${reaction.to.name}:".link("https://t.me/c/${reaction.to.chat.id}/${reaction.messageId}")
    }
}