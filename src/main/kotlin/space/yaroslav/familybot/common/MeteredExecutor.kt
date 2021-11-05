package space.yaroslav.familybot.common

import io.micrometer.core.instrument.MeterRegistry
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.Priority
import space.yaroslav.familybot.telegram.FamilyBot

fun Executor.meteredExecute(
    executorContext: ExecutorContext,
    meterRegistry: MeterRegistry
): suspend (AbsSender) -> Unit {

    return meterRegistry
        .timer("executors.${this::class.simpleName}.execute")
        .recordCallable {
            this.execute(executorContext)
        } ?: throw FamilyBot.InternalException("Something has gone wrong while calling metered executor")
}

fun Executor.meteredCanExecute(executorContext: ExecutorContext, meterRegistry: MeterRegistry): Boolean {
    return meterRegistry
        .timer("executors.${this::class.simpleName}.canExecute")
        .recordCallable {
            this.canExecute(executorContext)
        } ?: throw FamilyBot.InternalException("Something has gone wrong while calling metered executor")
}

fun Executor.meteredPriority(
    executorContext: ExecutorContext,
    meterRegistry: MeterRegistry
): Priority {
    return meterRegistry
        .timer("executors.${this::class.simpleName}.priority")
        .recordCallable {
            this.priority(executorContext)
        } ?: throw FamilyBot.InternalException("Something has gone wrong while calling metered executor")
}
