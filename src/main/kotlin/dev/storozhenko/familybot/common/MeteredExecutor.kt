package dev.storozhenko.familybot.common

import dev.storozhenko.familybot.core.executors.Executor
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority
import dev.storozhenko.familybot.core.telegram.FamilyBot
import io.micrometer.core.instrument.MeterRegistry
import org.telegram.telegrambots.meta.bots.AbsSender

fun Executor.meteredExecute(
    context: ExecutorContext,
    meterRegistry: MeterRegistry
): suspend (AbsSender) -> Unit {
    return meterRegistry
        .timer("executors.${this::class.simpleName}.execute")
        .recordCallable {
            this.execute(context)
        } ?: throw FamilyBot.InternalException("Something has gone wrong while calling metered executor")
}

fun Executor.meteredCanExecute(context: ExecutorContext, meterRegistry: MeterRegistry): Boolean {
    return meterRegistry
        .timer("executors.${this::class.simpleName}.canExecute")
        .recordCallable {
            this.canExecute(context)
        } ?: throw FamilyBot.InternalException("Something has gone wrong while calling metered executor")
}

fun Executor.meteredPriority(
    context: ExecutorContext,
    meterRegistry: MeterRegistry
): Priority {
    return meterRegistry
        .timer("executors.${this::class.simpleName}.priority")
        .recordCallable {
            this.priority(context)
        } ?: throw FamilyBot.InternalException("Something has gone wrong while calling metered executor")
}
