package dev.storozhenko.familybot.feature.reactions

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.PluralizedWordsProvider
import dev.storozhenko.familybot.common.extensions.bold
import dev.storozhenko.familybot.common.extensions.link
import dev.storozhenko.familybot.common.extensions.pluralize
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.ContinuousConversationExecutor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.models.telegram.User
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.telegram.FamilyBot
import dev.storozhenko.familybot.feature.settings.models.ChatGPTPaidTill
import dev.storozhenko.familybot.feature.settings.models.ChatGPTReactionsCooldown
import dev.storozhenko.familybot.feature.talking.services.TalkingServiceChatGpt
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import java.time.Instant
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Component
class ReactionsStatsContinuousExecutor(
    private val reactionRepository: ReactionRepository,
    private val chatGpt: TalkingServiceChatGpt,
    private val easyKeyValueService: EasyKeyValueService,
    botConfig: BotConfig
) :
    ContinuousConversationExecutor(botConfig) {
    override fun getDialogMessages(context: ExecutorContext) = setOf("За какой период реакции?")

    override fun command() = Command.REACTION_STATS

    private val pluralizedReactions = PluralizedWordsProvider({ "реакция" }, { "реакции" }, { "реакций" })

    override suspend fun execute(context: ExecutorContext) {
        val callbackQuery = context.update.callbackQuery
        context.sender.execute(AnswerCallbackQuery(callbackQuery.id))
        val callbackPeriod = callbackQuery.data
        context.sender.execute(
            DeleteMessage.builder().chatId(context.chat.idString).messageId(context.message.messageId).build()
        )
        val (reactionsPeriod, isAi) = ReactionsPeriod.parse(callbackPeriod)
        val reactions = reactionRepository.get(context.chat, reactionsPeriod.period)
        if (isAi) {
            sendAiReactions(context, reactionsPeriod, reactions)
        } else {
            sendRawReactions(context, reactionsPeriod, reactions)
        }
    }

    suspend fun sendRawReactions(
        context: ExecutorContext,
        period: ReactionsPeriod,
        reactions: List<ReactionRepository.Reaction>
    ) {

        val periodDesc = when (period) {
            ReactionsPeriod.WEEK -> "неделю"
            else -> period.periodName
        }

        context.sender.send(
            context,
            "Реакции за $periodDesc:".bold() + "\n${calculateReactionStats(reactions)}", enableHtml = true
        )
        context.sender.send(context, calculateReactionStatsByMessage(reactions), enableHtml = true)
    }

    suspend fun sendAiReactions(
        context: ExecutorContext,
        reactionsPeriod: ReactionsPeriod,
        reactions: List<ReactionRepository.Reaction>
    ) {
        val paidTill = easyKeyValueService.get(ChatGPTPaidTill, context.chatKey)
        val isCooldown = easyKeyValueService.get(ChatGPTReactionsCooldown, context.chatKey, false)
        if (paidTill == null || paidTill.isBefore(Instant.now())) {
            if (isCooldown) {
                context.sender.send(
                    context,
                    "АИ реакции на кулдауне, кулдаун 12 часов, если есть подписка из /shop то кулдаун там 5 минут"
                )
                return
            } else {
                easyKeyValueService.put(ChatGPTReactionsCooldown, context.chatKey, true, 12.hours)
            }
        } else {
            if (isCooldown) {
                context.sender.send(context, "Падажжи, кулдаун, всего 5 минут")
                return
            } else {
                easyKeyValueService.put(ChatGPTReactionsCooldown, context.chatKey, true, 5.minutes)
            }
        }
        val reactionStats = calculateReactionStats(reactions)
        val promptPrefix = when (reactionsPeriod) {
            ReactionsPeriod.DAY -> "за последние сутки"
            ReactionsPeriod.WEEK -> "за последнюю неделю"
            ReactionsPeriod.MONTH -> "за последний месяц"
        }
        val analytics = chatGpt.internalMessage(
            """
                Ниже я передам список людей и количество реакций на свои сообщения которые они получили $promptPrefix.
                Построй веселую и забавную аналитику с матом и грубостями.
                Задача аналитики это быть отправленной чат и собрать смех.
                В ответе нельзя использовать html теги.
                Список реакций:
                $reactionStats
            """.trimIndent()
        )
        context.sender.send(context, analytics)
        return
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