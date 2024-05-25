package com.linhnvtit.fun_cache.utils

import java.util.concurrent.atomic.AtomicBoolean

internal object FunCacheLog {
    var enabled: AtomicBoolean = AtomicBoolean(false)

    fun d(message: String) {
        if (enabled.get()) {
            Thread.currentThread().stackTrace[3].let {
                println("[$FUN_CACHE][${it.fileName}:${it.lineNumber}] [Debug]: $message")
            }
        }
    }

    fun e(message: String) {
        if (enabled.get()) {
            Thread.currentThread().stackTrace[3].let {
                println("[$FUN_CACHE][${it.fileName}:${it.lineNumber}] [Error]: $message")
            }
        }
    }

    fun network(message: String) {
        if (enabled.get()) {
            Thread.currentThread().stackTrace[3].let {
                println("[$FUN_CACHE][${it.fileName}:${it.lineNumber}] [Network]: $message")
            }
        }
    }
}