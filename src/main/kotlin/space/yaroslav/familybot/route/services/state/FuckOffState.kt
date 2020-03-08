package space.yaroslav.familybot.route.services.state

import java.time.Duration
import space.yaroslav.familybot.route.models.FunctionId

class FuckOffState(duration: Duration) : TimeLimitedState(duration), FunctionalToleranceState {

    override fun additionalIsOverChecks(): List<() -> Boolean> {
        return emptyList()
    }

    override fun disabledFunctionIds() = setOf(FunctionId.CHATTING, FunctionId.HUIFICATE)
}
