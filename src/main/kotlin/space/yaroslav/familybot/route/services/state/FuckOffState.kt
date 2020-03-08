package space.yaroslav.familybot.route.services.state

import space.yaroslav.familybot.route.models.FunctionId
import java.time.Duration

class FuckOffState(duration: Duration) : TimeLimitedState(duration), FunctionalToleranceState {

    override fun additionalIsOverChecks(): List<() -> Boolean> {
        return emptyList()
    }

    override fun disabledFunctionIds() = setOf(FunctionId.CHATTING, FunctionId.HUIFICATE)


}