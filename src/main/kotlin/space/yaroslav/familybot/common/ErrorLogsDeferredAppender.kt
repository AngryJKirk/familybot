package space.yaroslav.familybot.common

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.StackTraceElementProxy
import ch.qos.logback.core.Appender
import ch.qos.logback.core.Context
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import ch.qos.logback.core.status.Status
import space.yaroslav.familybot.common.extensions.prettyFormat
import java.time.Instant

class ErrorLogsDeferredAppender : Appender<ILoggingEvent> {
    private var context: Context? = null
    private var name: String? = null

    companion object {
        val errors: MutableList<String> = mutableListOf()
    }

    override fun start() {
        // useless method for this appender
    }

    override fun stop() {
        // useless method for this appender
    }

    override fun isStarted() = true

    override fun setContext(context: Context?) {
        this.context = context
    }

    override fun getContext() = context

    override fun addStatus(status: Status?) {
        // useless method for this appender
    }

    override fun addInfo(msg: String?) {
        // useless method for this appender
    }

    override fun addInfo(msg: String?, ex: Throwable?) {
        // useless method for this appender
    }

    override fun addWarn(msg: String?) {
        // useless method for this appender
    }

    override fun addWarn(msg: String?, ex: Throwable?) {
        // useless method for this appender
    }

    override fun addError(msg: String?) {
        // useless method for this appender
    }

    override fun addError(msg: String?, ex: Throwable?) {
        // useless method for this appender
    }

    override fun addFilter(newFilter: Filter<ILoggingEvent>?) {
        // useless method for this appender
    }

    override fun clearAllFilters() {
        // useless method for this appender
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
                val stackTrace = event.throwableProxy.stackTraceElementProxyArray.joinToString(
                    separator = "\n",
                    transform = StackTraceElementProxy::getSTEAsString
                )
                "\nException: $className : $message \nStack trace:\n$stackTrace"
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
