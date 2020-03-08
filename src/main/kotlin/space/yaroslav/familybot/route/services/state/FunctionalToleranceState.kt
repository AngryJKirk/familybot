package space.yaroslav.familybot.route.services.state

import space.yaroslav.familybot.route.models.FunctionId

interface FunctionalToleranceState : State {

     fun disabledFunctionIds(): Set<FunctionId>

}