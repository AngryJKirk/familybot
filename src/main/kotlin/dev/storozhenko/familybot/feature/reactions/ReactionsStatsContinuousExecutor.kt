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
import dev.storozhenko.familybot.feature.settings.models.ChatGPT4Enabled
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
        context.client.execute(AnswerCallbackQuery(callbackQuery.id))
        val callbackPeriod = callbackQuery.data
        context.client.execute(
            DeleteMessage.builder().chatId(context.chat.idString).messageId(context.message.messageId).build()
        )
        val (reactionsPeriod, isAi) = ReactionsPeriod.parse(callbackPeriod)
        val reactions = reactionRepository.get(context.chat, reactionsPeriod.period)
        if (reactions.isEmpty()) {
            context.client.send(
                context,
                "Реакций еще нет. Скорее всего, вам необходимо сделать бота админом чтобы он имел возможность собирать реакции."
            )
            return
        }
        if (isAi) {
            sendAiReactions(context, reactionsPeriod, reactions)
        } else {
            sendRawReactions(context, reactionsPeriod, reactions)
        }
    }

    private suspend fun sendRawReactions(
        context: ExecutorContext,
        period: ReactionsPeriod,
        reactions: List<ReactionRepository.Reaction>
    ) {

        context.client.send(
            context,
            getPeriodDesc(period).bold() + "\n${calculateReactionStats(reactions)}", enableHtml = true
        )
        context.client.send(context, calculateReactionStatsByMessage(reactions), enableHtml = true)
    }

    private suspend fun sendAiReactions(
        context: ExecutorContext,
        period: ReactionsPeriod,
        reactions: List<ReactionRepository.Reaction>
    ) {
        val paidTill = easyKeyValueService.get(ChatGPTPaidTill, context.chatKey)
        val isCooldown = easyKeyValueService.get(ChatGPTReactionsCooldown, context.chatKey, false)
        if (paidTill == null || paidTill.isBefore(Instant.now())) {
            if (isCooldown) {
                context.client.send(
                    context,
                    "АИ реакции на кулдауне, кулдаун 12 часов, если есть подписка из /shop то кулдаун там 5 минут"
                )
                return
            } else {
                easyKeyValueService.put(ChatGPTReactionsCooldown, context.chatKey, true, 12.hours)
            }
        } else {
            if (isCooldown) {
                context.client.send(context, "Падажжи, кулдаун, всего 5 минут")
                return
            } else {
                easyKeyValueService.put(ChatGPTReactionsCooldown, context.chatKey, true, 5.minutes)
            }
        }
        val reactionStats = calculateReactionStats(reactions, htmlFree = true)
        val promptPrefix = when (period) {
            ReactionsPeriod.DAY -> "за последние сутки"
            ReactionsPeriod.WEEK -> "за последнюю неделю"
            ReactionsPeriod.MONTH -> "за последний месяц"
        }
        val useChatGpt4 = easyKeyValueService.get(ChatGPT4Enabled, context.chatKey, false)
        val analytics = chatGpt.internalMessage(
            """
                Ниже я передам список людей и количество реакций на свои сообщения которые они получили $promptPrefix.
                Построй веселую и забавную аналитику с матом и грубостями.
                Задача аналитики это быть отправленной чат и собрать смех.
                Запрещено использовать html и markdown.
                Список реакций:
                $reactionStats
            """.trimIndent(),
            useChatGpt4
        )
        context.client.send(context, getPeriodDesc(period) + "\n" + analytics)
        return
    }

    private fun calculateReactionStatsByMessage(reactions: List<ReactionRepository.Reaction>): String {
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

    private fun calculateReactionStats(
        reactionsData: List<ReactionRepository.Reaction>,
        htmlFree: Boolean = false
    ): String {
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
                    totalUserReactions[user] ?: throw FamilyBot.InternalException("some fuck up?"),
                    htmlFree
                )
            }


    }

    private fun getPeriodDesc(period: ReactionsPeriod): String {
        val periodDesc = when (period) {
            ReactionsPeriod.WEEK -> "неделю"
            else -> period.periodName
        }
        return "Отчет за $periodDesc:"
    }

    private fun formatMessage(
        user: User,
        reactionCounter: Int,
        totalUserReactions: List<String>,
        htmlFree: Boolean
    ): String {
        val generalName = user.getGeneralName(mention = false)
        val name = if (htmlFree) generalName else generalName.bold()
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