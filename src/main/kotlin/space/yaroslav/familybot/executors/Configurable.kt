package space.yaroslav.familybot.executors

import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.models.router.FunctionId

interface Configurable {

    fun getFunctionId(update: Update): FunctionId
}
