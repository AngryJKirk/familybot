package space.yaroslav.familybot.common.utils

import io.micrometer.core.instrument.MeterRegistry
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.router.Priority

fun Executor.meteredExecute(update: Update, meterRegistry: MeterRegistry): suspend (AbsSender) -> Unit {

    return meterRegistry
        .timer("executors.${this::class.simpleName}.execute")
        .recordCallable {
            this.execute(update)
        }
}

fun Executor.meteredCanExecute(message: Message, meterRegistry: MeterRegistry): Boolean {
    return meterRegistry
        .timer("executors.${this::class.simpleName}.canExecute")
        .recordCallable {
            this.canExecute(message)
        }
}

fun Executor.meteredPriority(update: Update, meterRegistry: MeterRegistry): Priority {
    return meterRegistry
        .timer("executors.${this::class.simpleName}.priority")
        .recordCallable {
            this.priority(update)
        }
}
