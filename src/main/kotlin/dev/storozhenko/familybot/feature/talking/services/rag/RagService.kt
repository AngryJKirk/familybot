package dev.storozhenko.familybot.feature.talking.services.rag

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.embedding.Embedding
import com.aallam.openai.api.embedding.EmbeddingRequest
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.prettyFormat
import dev.storozhenko.familybot.core.repos.UserRepository
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.telegram.FamilyBot
import dev.storozhenko.familybot.feature.talking.models.RagHit
import dev.storozhenko.familybot.feature.talking.repos.RagRepository
import dev.storozhenko.familybot.feature.talking.services.AiService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.seconds

@Component
class RagService(
    private val ragRepository: RagRepository,
    private val userRepository: UserRepository,
    private val aiService: AiService,
    private val botConfig: BotConfig,
) {

    private val log = KotlinLogging.logger {}

    suspend fun add(context: ExecutorContext) {
        if (context.message.messageId == null) return

        try {
            val imageDesc = aiService.getImageDescription(context)

            if (imageDesc != null) {
                log.info { "Adding image to rag" }
                val text = if (isTextValid(context)) "с подписью <${context.message.caption}>" else ""
                ragRepository.add(
                    context,
                    getEmbedding(imageDesc).first(),
                    textOverride = "Пользователь прислал изображение $text: [$imageDesc]"
                )
            } else {
                if (!isTextValid(context)) return
                val embeddings = getEmbedding(context.message.text ?: context.message.caption)
                log.info { "Adding new message to rag" }
                ragRepository.add(context, embeddings.first())
            }

        } catch (e: Exception) {
            log.error(e) { "Rag adding failed" }
        }

    }

    private fun isTextValid(context: ExecutorContext): Boolean {
        val text = context.message.text ?: context.message.caption ?: return false
        if (text.isBlank()) return false
        if (text.length <= 3) return false
        if (text.startsWith("/")) return false
        return true
    }

    private fun cleanupBotMentions(text: String?): String? {
        if (text == null) return null
        var clean: String  = text
        for (alias in botConfig.botNameAliases) {
            clean = clean.replace(alias, "", ignoreCase = true)
        }
        return clean.replace(botConfig.botName, "", ignoreCase = true)
    }

    suspend fun getContext(context: ExecutorContext, chatMessages: MutableList<ChatMessage>): String {
        try {
            val text = cleanupBotMentions(context.message.text) ?: return ""
            val semantic =
                coroutineScope {
                    async {
                        val previousMessages = chatMessages.takeLast(10)
                            .mapNotNull(ChatMessage::content)
                            .joinToString(separator = "\n") { it.split("  говорит:").last() }

                        ragRepository.searchSemantic(context, getEmbedding("$previousMessages\n$text").first())
                    }
                }
            val keywordRu = coroutineScope { async { ragRepository.searchKeywordRu(context, text) } }
            val keywordSimple = coroutineScope { async { ragRepository.searchKeywordSimple(context, text) } }
            val fuzzy = coroutineScope { async { ragRepository.searchFuzzy(context, text) } }
            val recent = coroutineScope { async { ragRepository.recentWindow(context) } }
            return generateContext(
                mergeAndRerank(
                    semantic.await(),
                    keywordRu.await(),
                    keywordSimple.await(),
                    recent.await(),
                    fuzzy.await(),

                    )
            )
        } catch (e: Exception) {
            log.error(e) { "Rag retrieval failed" }
            return ""
        }

    }

    private fun generateContext(ragHit: List<RagHit>): String {
        val userIds = ragHit.map(RagHit::userId)
        val userNames = userRepository.getUserNamesById(userIds)
        return ragHit
            .sortedWith(compareBy(RagHit::ts).thenBy(RagHit::msgId).thenBy(RagHit::ragId))
            .joinToString(separator = "\n", prefix = "Это релевантные сообщения:\n") { hit ->
                "- [${hit.ts.prettyFormat()}|${hit.kind}]  @(${userNames[hit.userId] ?: "N/A"}) ${hit.text}"
            }
    }

    private var openAI: OpenAI? = null
    private fun getOpenAIService(): OpenAI {
        if (openAI == null) {
            val token = botConfig.aiToken
                ?: throw FamilyBot.InternalException("Open AI token is not available, check config")
            openAI = OpenAI(
                token = token,
                timeout = Timeout(socket = 60.seconds),
                logging = LoggingConfig(logLevel = LogLevel.None)
            )
        }
        return openAI as OpenAI
    }

    private suspend fun getEmbedding(text: String): List<Embedding> {
        return getOpenAIService().embeddings(
            EmbeddingRequest(
                ModelId("text-embedding-3-small"),
                listOf(text)
            )
        ).embeddings
    }

}