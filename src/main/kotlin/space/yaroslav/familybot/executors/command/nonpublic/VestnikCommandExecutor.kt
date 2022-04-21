package space.yaroslav.familybot.executors.command.nonpublic

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.sendDeferred
import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.repos.AskWorldRepository
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.UkrainianLanguage
import space.yaroslav.familybot.services.talking.TranslateService

@Component
class VestnikCommandExecutor(
    private val askWorldRepository: AskWorldRepository,
    private val translateService: TranslateService,
    private val easyKeyValueService: EasyKeyValueService,
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