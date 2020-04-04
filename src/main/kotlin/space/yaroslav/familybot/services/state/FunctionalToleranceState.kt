package space.yaroslav.familybot.services.state

import space.yaroslav.familybot.models.FunctionId

interface FunctionalToleranceState : State {

    fun disabledFunctionIds(): Set<FunctionId>
}
