package com.stack.core.crash

interface CrashCapture {
    fun install()
}

class CrashCaptureImpl(
    private val onCrash: (Thread, Throwable) -> Unit
) : CrashCapture {

    override fun install() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            onCrash(thread, throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
