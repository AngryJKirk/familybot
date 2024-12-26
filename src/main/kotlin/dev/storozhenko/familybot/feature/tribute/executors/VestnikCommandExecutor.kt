package dev.storozhenko.familybot.feature.tribute.executors

import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.askworld.repos.AskWorldRepository
import dev.storozhenko.familybot.feature.settings.models.UkrainianLanguage
import dev.storozhenko.familybot.feature.talking.services.TranslateService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
class VestnikCommandExecutor(
    private val askWorldRepository: AskWorldRepository,
    private val translateService: TranslateService,
    private val easyKeyValueService: EasyKeyValueService,
) : CommandExecutor() {
    private val chat = Chat(id = -1001351771258L, name = null)
    override suspend fun execute(context: ExecutorContext) {
        val question = coroutineScope {
            async {
                val isUkrainian = async { easyKeyValueService.get(UkrainianLanguage, context.chatKey, false) }
                val question =
                    askWorldRepository.searchQuestion("вестник", chat).randomOrNull()?.message ?: "Выпусков нет :("
                if (isUkrainian.await()) {
                    translateService.translate(question)
                } else {
                    question
                }
            }
        }

        context.send(context.phrase(Phrase.RANDOM_VESTNIK))
        context.sendDeferred(question, shouldTypeBeforeSend = true)
    }

    override fun command() = Command.VESTNIK
}
