package space.yaroslav.familybot.services.state

import space.yaroslav.familybot.models.FunctionId
import java.time.Duration

class FuckOffState(duration: Duration) : TimeLimitedState(duration), FunctionalToleranceState {

    override fun additionalIsOverChecks(): List<() -> Boolean> {
        return emptyList()
    }

    override fun disabledFunctionIds() = setOf(FunctionId.CHATTING, FunctionId.HUIFICATE)
}
