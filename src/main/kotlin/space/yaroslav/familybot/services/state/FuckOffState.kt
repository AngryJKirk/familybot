package space.yaroslav.familybot.services.state

import java.time.Duration
import space.yaroslav.familybot.models.FunctionId

class FuckOffState(duration: Duration) : TimeLimitedState(duration), FunctionalToleranceState {

    override fun additionalIsOverChecks(): List<() -> Boolean> {
        return emptyList()
    }

    override fun disabledFunctionIds() = setOf(FunctionId.CHATTING, FunctionId.HUIFICATE)
}
