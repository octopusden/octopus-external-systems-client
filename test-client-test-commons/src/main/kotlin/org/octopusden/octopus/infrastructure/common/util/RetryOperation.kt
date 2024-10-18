package org.octopusden.octopus.infrastructure.common.util

import org.slf4j.LoggerFactory


class RetryOperation<T> {
    companion object {
        private val logger by lazy { LoggerFactory.getLogger(RetryOperation::class.java) }

        @JvmStatic
        fun <T> configure(init: RetryOperation<T>.() -> Unit): RetryOperation<T> {
            val retryOperation = RetryOperation<T>()
            retryOperation.init()
            return retryOperation
        }
    }

    var attempts: Int = 0
    private var failureExceptions: (exception: Exception) -> Boolean = { false }
    private var onExceptionLogFunction: ((exception: Exception, attempt: Int) -> String) = { e, a ->
        val message = "Retrying on ${e.javaClass.name}, attempt=$a"
        logger.warn(message)
        message
    }
    private var onFailFunction: (message: String) -> Unit = { }

    fun failureException(function: (exception: Exception) -> Boolean) {
        this.failureExceptions = function
    }
    fun onException(function: (exception: Exception, attempt: Int) -> String) {
        this.onExceptionLogFunction = function
    }
    fun executeOnFail(function: (message: String) -> Unit) {
        onFailFunction = function
    }

    fun execute(function: () -> T): T {
        var a = 1
        while (true) {
            try {
                return function()
            } catch (e: Exception) {
                if (a < attempts && failureExceptions.invoke(e)) {
                    onFailFunction.invoke(onExceptionLogFunction.invoke(e, a))
                } else {
                    throw e
                }
            }
            a++
        }
    }
}