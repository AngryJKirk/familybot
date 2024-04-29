package dev.storozhenko.familybot.feature.reactions

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.PluralizedWordsProvider
import dev.storozhenko.familybot.common.extensions.bold
import dev.storozhenko.familybot.common.extensions.link
import dev.storozhenko.familybot.common.extensions.pluralize
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.ContiniousConversationExecutor
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.models.telegram.User
import dev.storozhenko.familybot.core.repos.ReactionRepository
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.telegram.FamilyBot
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class ReactionsStatsContiniousExecutor(private val reactionRepository: ReactionRepository, botConfig: BotConfig) :
    ContiniousConversationExecutor(botConfig) {
    override fun getDialogMessages(context: ExecutorContext) = setOf("За какой период реакции?")

    override fun command() = Command.REACTION_STATS

    private val pluralizedReactions = PluralizedWordsProvider({ "реакция" }, { "реакции" }, { "реакций" })

    override suspend fun execute(context: ExecutorContext) {
        val callbackQuery = context.update.callbackQuery
        context.sender.execute(AnswerCallbackQuery(callbackQuery.id))
        val message = callbackQuery.data
        val after = when (message) {
            "день" -> Instant.now().minus(24, ChronoUnit.HOURS)
            "неделя" -> Instant.now().minus(7, ChronoUnit.DAYS)
            "месяц" -> Instant.now().minus(30, ChronoUnit.DAYS)
            else -> Instant.now().minus(24, ChronoUnit.HOURS)
        }
        val reactions = reactionRepository.get(context.chat, after)


        context.sender.send(context, calculateReactionStats(reactions), enableHtml = true)
        context.sender.send(context, calculateReactionStatsByMessage(reactions), enableHtml = true)
    }

    fun calculateReactionStatsByMessage(reactions: List<ReactionRepository.Reaction>): String {
        return reactions
            .map { messageLink(it) to it.reactions.size }
            .groupBy { (messageId, _) -> messageId }
            .mapValues { (_, count) -> count.sumOf(Pair<String, Int>::second) }
            .entries
            .asSequence()
            .sortedByDescending { (_, count) -> count }
            .filter { (_, count) -> count > 2 }
            .take(10)
            .mapIndexed { index, entry ->
                "${index + 1}. ${entry.key} ${
                    (entry.value.toString() + " " + pluralize(
                        entry.value,
                        pluralizedReactions
                    )).bold()
                } "
            }
            .joinToString("\n")
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
        val chatId = reaction.to.chat.id.toString().replace("-100", "")
        return "от ${reaction.to.name}:".link("https://t.me/c/$chatId/${reaction.messageId}")
    }
}