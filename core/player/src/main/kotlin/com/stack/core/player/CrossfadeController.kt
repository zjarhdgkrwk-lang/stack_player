package com.stack.core.player

import android.os.Handler
import android.os.Looper
import com.stack.core.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrossfadeController @Inject constructor(
    private val playerManager: StackPlayerManager,
    private val stateReducer: PlaybackStateReducer
) {
    companion object {
        private const val TAG = "CrossfadeController"
        private const val FADE_INTERVAL_MS = 50L
    }

    private val handler = Handler(Looper.getMainLooper())
    private var isCrossfading = false
    private var fadeRunnable: Runnable? = null

    fun checkAndStartCrossfade(positionMs: Long, durationMs: Long) {
        if (isCrossfading) return
        val crossfadeDuration = stateReducer.currentState.crossfadeDurationMs
        if (crossfadeDuration == 0) return
        if (durationMs <= 0) return

        val triggerPoint = durationMs - kotlin.math.abs(crossfadeDuration)
        if (positionMs < triggerPoint) return

        startCrossfade(crossfadeDuration)
    }

    private fun startCrossfade(crossfadeDurationMs: Int) {
        isCrossfading = true
        val absDuration = kotlin.math.abs(crossfadeDurationMs)
        Logger.d(TAG, "Starting crossfade: ${crossfadeDurationMs}ms")

        playerManager.startWarmPlayer()

        when {
            // Standard crossfade (positive duration): fade out active, fade in warm
            crossfadeDurationMs > 0 -> {
                val steps = (absDuration / FADE_INTERVAL_MS).toInt().coerceAtLeast(1)
                var step = 0

                fadeRunnable = object : Runnable {
                    override fun run() {
                        if (step >= steps) {
                            completeCrossfade()
                            return
                        }
                        val progress = step.toFloat() / steps
                        playerManager.setActiveVolume(1f - progress)
                        playerManager.setWarmVolume(progress)
                        step++
                        handler.postDelayed(this, FADE_INTERVAL_MS)
                    }
                }
                handler.post(fadeRunnable!!)
            }
            // Overlap (negative duration): warm plays at full volume alongside active
            crossfadeDurationMs < 0 -> {
                playerManager.setWarmVolume(1f)
                handler.postDelayed({
                    completeCrossfade()
                }, absDuration.toLong())
            }
        }
    }

    private fun completeCrossfade() {
        playerManager.setActiveVolume(0f)
        playerManager.setWarmVolume(1f)
        playerManager.swapPlayers()
        playerManager.setActiveVolume(1f)
        isCrossfading = false
        Logger.d(TAG, "Crossfade complete, players swapped")
    }

    fun cancelCrossfade() {
        fadeRunnable?.let { handler.removeCallbacks(it) }
        fadeRunnable = null
        isCrossfading = false
        playerManager.setActiveVolume(1f)
    }

    fun isActive(): Boolean = isCrossfading
}
