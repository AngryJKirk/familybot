package dev.storozhenko.familybot.feature.scenario.executors

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.isFromAdmin
import dev.storozhenko.familybot.core.executors.ContiniousConversationExecutor
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.telegram.FamilyBot
import dev.storozhenko.familybot.services.scenario.ScenarioService
import dev.storozhenko.familybot.services.scenario.ScenarioSessionManagementService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery

@Component
class ScenarioContiniousExecutor(
    private val scenarioSessionManagementService: ScenarioSessionManagementService,
    private val scenarioService: ScenarioService,
    botConfig: BotConfig
) :
    ContiniousConversationExecutor(botConfig) {
    override fun getDialogMessages(context: ExecutorContext) = context.allPhrases(Phrase.SCENARIO_CHOOSE)

    override fun command() = Command.SCENARIO

    override suspend fun execute(context: ExecutorContext) {
        val callbackQuery = context.update.callbackQuery

        if (!context.sender.isFromAdmin(context)) {
            context.sender.execute(
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
            scenarioSessionManagementService.startGame(context, scenarioToStart)
        }
    }
}
