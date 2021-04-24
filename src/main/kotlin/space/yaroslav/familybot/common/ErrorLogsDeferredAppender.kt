package space.yaroslav.familybot.common

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import ch.qos.logback.core.Context
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import ch.qos.logback.core.status.Status
import space.yaroslav.familybot.common.utils.prettyFormat
import java.time.Instant

class ErrorLogsDeferredAppender : Appender<ILoggingEvent> {
    private var context: Context? = null
    private var name: String? = null

    companion object {
        val errors: MutableList<String> = mutableListOf()
    }

    override fun start() {
    }

    override fun stop() {
    }

    override fun isStarted() = true

    override fun setContext(context: Context?) {
        this.context = context
    }

    override fun getContext() = context

    override fun addStatus(status: Status?) {
    }

    override fun addInfo(msg: String?) {
    }

    override fun addInfo(msg: String?, ex: Throwable?) {
    }

    override fun addWarn(msg: String?) {
    }

    override fun addWarn(msg: String?, ex: Throwable?) {
    }

    override fun addError(msg: String?) {
    }

    override fun addError(msg: String?, ex: Throwable?) {
    }

    override fun addFilter(newFilter: Filter<ILoggingEvent>?) {
    }

    override fun clearAllFilters() {
    }

    override fun getCopyOfAttachedFiltersList(): MutableList<Filter<ILoggingEvent>> {
        return mutableListOf()
    }

    override fun getFilterChainDecision(event: ILoggingEvent?) = FilterReply.NEUTRAL

    override fun getName() = name

    override fun doAppend(event: ILoggingEvent?) {
        if (event != null && event.level == Level.ERROR) {
            val exceptionMessage = if (event.throwableProxy != null) {
                val className = event.throwableProxy.className
                val message = event.throwableProxy.message
                "\nException: $className : $message"
            } else {
                ""
            }

            val date = Instant.ofEpochMilli(event.timeStamp).prettyFormat()
            errors.add(
                date +
                    "\n" +
                    event.formattedMessage +
                    exceptionMessage +
                    "\n" +
                    "MDC: ${event.mdcPropertyMap}"
            )
        }
    }

    override fun setName(name: String?) {
        this.name = name
    }
}
