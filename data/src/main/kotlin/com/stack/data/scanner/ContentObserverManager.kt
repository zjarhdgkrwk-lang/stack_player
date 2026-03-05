package com.stack.data.scanner

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.stack.core.logging.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentObserverManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scanManager: ScanManager
) {
    companion object {
        private const val TAG = "ContentObserverManager"
        private const val DEBOUNCE_MS = 3000L
    }

    private var observer: ContentObserver? = null
    private var debounceJob: Job? = null
    private var scope: CoroutineScope? = null

    /**
     * Start observing MediaStore audio changes.
     * Changes are debounced by 3 seconds before triggering an incremental scan.
     */
    fun startObserving(coroutineScope: CoroutineScope) {
        scope = coroutineScope
        if (observer != null) return

        observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                onChange(selfChange, null)
            }

            override fun onChange(selfChange: Boolean, uri: Uri?) {
                Logger.d(TAG, "MediaStore change detected: $uri")
                scheduleIncrementalScan()
            }
        }

        context.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            observer!!
        )

        Logger.i(TAG, "Started observing MediaStore changes")
    }

    fun stopObserving() {
        observer?.let {
            context.contentResolver.unregisterContentObserver(it)
        }
        observer = null
        debounceJob?.cancel()
        debounceJob = null
        scope = null
        Logger.i(TAG, "Stopped observing MediaStore changes")
    }

    private fun scheduleIncrementalScan() {
        debounceJob?.cancel()
        debounceJob = scope?.launch {
            delay(DEBOUNCE_MS)
            Logger.i(TAG, "Debounce elapsed, triggering incremental scan")
            scanManager.incrementalScan()
        }
    }
}
