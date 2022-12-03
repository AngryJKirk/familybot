package dev.storozhenko.familybot.executors.command.nonpublic

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.common.extensions.sendDeferred
import dev.storozhenko.familybot.executors.command.CommandExecutor
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.telegram.Chat
import dev.storozhenko.familybot.models.telegram.Command
import dev.storozhenko.familybot.repos.AskWorldRepository
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.services.settings.UkrainianLanguage
import dev.storozhenko.familybot.services.talking.TranslateService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class VestnikCommandExecutor(
    private val askWorldRepository: AskWorldRepository,
    private val translateService: TranslateService,
    private val easyKeyValueService: EasyKeyValueService
) : CommandExecutor() {
    private val chat = Chat(id = -1001351771258L, name = null)
    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        return { sender ->
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

            sender.send(context, context.phrase(Phrase.RANDOM_VESTNIK))
            sender.sendDeferred(context, question, shouldTypeBeforeSend = true)
        }
    }

    override fun command() = Command.VESTNIK
}
