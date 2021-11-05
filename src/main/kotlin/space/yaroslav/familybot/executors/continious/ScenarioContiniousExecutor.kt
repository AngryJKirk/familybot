package space.yaroslav.familybot.executors.continious

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.isFromAdmin
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.services.scenario.ScenarioService
import space.yaroslav.familybot.services.scenario.ScenarioSessionManagementService
import space.yaroslav.familybot.telegram.BotConfig
import space.yaroslav.familybot.telegram.FamilyBot

@Component
class ScenarioContiniousExecutor(
    private val scenarioSessionManagementService: ScenarioSessionManagementService,
    private val scenarioService: ScenarioService,
    private val botConfig: BotConfig
) :
    ContiniousConversationExecutor(botConfig) {
    override fun getDialogMessage(context: ExecutorContext) = "Какую игру выбрать?"

    override fun command() = Command.SCENARIO

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        return {
            val callbackQuery = context.update.callbackQuery

            if (!it.isFromAdmin(context)) {
                it.execute(
                    AnswerCallbackQuery(callbackQuery.id)
                        .apply {
                            showAlert = true
                            text = context.phrase(Phrase.ACCESS_DENIED)
                        }
                )
            } else {
                val scenarioToStart = scenarioService.getScenarios()
                    .find { (id) -> id.toString() == callbackQuery.data }
                    ?: throw FamilyBot.InternalException("Can't find a scenario ${callbackQuery.data}")
                scenarioSessionManagementService.startGame(context, scenarioToStart).invoke(it)
            }
        }
    }
}
