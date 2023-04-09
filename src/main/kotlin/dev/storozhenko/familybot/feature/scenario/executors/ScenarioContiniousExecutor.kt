package dev.storozhenko.familybot.feature.scenario.executors

import dev.storozhenko.familybot.common.extensions.isFromAdmin
import dev.storozhenko.familybot.core.executors.ContiniousConversationExecutor
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.services.scenario.ScenarioService
import dev.storozhenko.familybot.services.scenario.ScenarioSessionManagementService
import dev.storozhenko.familybot.telegram.BotConfig
import dev.storozhenko.familybot.telegram.FamilyBot
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class ScenarioContiniousExecutor(
    private val scenarioSessionManagementService: ScenarioSessionManagementService,
    private val scenarioService: ScenarioService,
    botConfig: BotConfig
) :
    ContiniousConversationExecutor(botConfig) {
    override fun getDialogMessages(context: ExecutorContext) = context.allPhrases(Phrase.SCENARIO_CHOOSE)

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
