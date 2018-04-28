package space.yaroslav.familybot.route.executors

import space.yaroslav.familybot.route.models.FunctionId

interface Configurable {

    fun getFunctionId(): FunctionId
}