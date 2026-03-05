package com.stack.player

import android.app.Application
import com.stack.core.logging.Logger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StackApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.init(isDebug = BuildConfig.DEBUG)
    }
}
