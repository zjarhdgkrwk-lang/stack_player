package com.stack.core.player

import android.media.audiofx.Equalizer
import com.stack.core.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EqualizerManager @Inject constructor() {

    companion object {
        private const val TAG = "EqualizerManager"
    }

    private var equalizer: Equalizer? = null
    private var isEnabled: Boolean = false

    val numberOfBands: Int
        get() = equalizer?.numberOfBands?.toInt() ?: 0

    val bandLevelRange: Pair<Short, Short>
        get() {
            val range = equalizer?.bandLevelRange
            return if (range != null && range.size >= 2) {
                range[0] to range[1]
            } else {
                (-1500).toShort() to 1500.toShort()
            }
        }

    val numberOfPresets: Int
        get() = equalizer?.numberOfPresets?.toInt() ?: 0

    fun attachToAudioSession(audioSessionId: Int) {
        release()
        if (audioSessionId == 0) return
        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = isEnabled
            }
            Logger.i(TAG, "Attached to audio session $audioSessionId, bands: $numberOfBands")
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to create equalizer", e)
        }
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        equalizer?.enabled = enabled
    }

    fun getBandLevel(band: Int): Short {
        return try {
            equalizer?.getBandLevel(band.toShort()) ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun setBandLevel(band: Int, level: Short) {
        try {
            equalizer?.setBandLevel(band.toShort(), level)
        } catch (e: Exception) {
            Logger.w(TAG, "Failed to set band level", e)
        }
    }

    fun getCenterFrequency(band: Int): Int {
        return try {
            equalizer?.getCenterFreq(band.toShort())?.div(1000) ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun getPresetName(preset: Int): String {
        return try {
            equalizer?.getPresetName(preset.toShort()) ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun usePreset(preset: Int) {
        try {
            equalizer?.usePreset(preset.toShort())
        } catch (e: Exception) {
            Logger.w(TAG, "Failed to use preset", e)
        }
    }

    fun flatten() {
        val eq = equalizer ?: return
        val bands = eq.numberOfBands.toInt()
        for (i in 0 until bands) {
            eq.setBandLevel(i.toShort(), 0)
        }
    }

    fun release() {
        try {
            equalizer?.release()
        } catch (_: Exception) {
        }
        equalizer = null
    }
}
