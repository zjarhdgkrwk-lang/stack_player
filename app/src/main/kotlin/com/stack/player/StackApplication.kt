package com.stack.player

import android.app.Application
import com.stack.core.logging.Logger
import com.stack.data.local.db.callback.SystemTagSeeder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class StackApplication : Application() {

    @Inject
    lateinit var systemTagSeeder: SystemTagSeeder

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Logger.init(isDebug = BuildConfig.DEBUG)

        applicationScope.launch {
            systemTagSeeder.seedIfNeeded()
        }
    }
}
