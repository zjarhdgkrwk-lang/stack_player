package com.stack.core.player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import com.stack.core.logging.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioFocusHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AudioFocusHandler"
        private const val DUCK_VOLUME = 0.2f
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var focusRequest: AudioFocusRequest? = null
    private var onFocusGained: (() -> Unit)? = null
    private var onFocusLost: (() -> Unit)? = null
    private var onDuck: ((Float) -> Unit)? = null
    private var wasPlayingBeforeLoss = false

    fun setCallbacks(
        onGained: () -> Unit,
        onLost: () -> Unit,
        onDuck: (Float) -> Unit
    ) {
        this.onFocusGained = onGained
        this.onFocusLost = onLost
        this.onDuck = onDuck
    }

    fun requestFocus(): Boolean {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(attrs)
            .setWillPauseWhenDucked(false)
            .setOnAudioFocusChangeListener { focusChange ->
                handleFocusChange(focusChange)
            }
            .build()

        focusRequest = request
        val result = audioManager.requestAudioFocus(request)
        val granted = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        Logger.d(TAG, "Focus request: ${if (granted) "granted" else "denied"}")
        return granted
    }

    fun abandonFocus() {
        focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        focusRequest = null
    }

    private fun handleFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                Logger.d(TAG, "Focus gained")
                onDuck?.invoke(1f)
                onFocusGained?.invoke()
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                Logger.d(TAG, "Focus lost permanently")
                onFocusLost?.invoke()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                Logger.d(TAG, "Focus lost transient")
                wasPlayingBeforeLoss = true
                onFocusLost?.invoke()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                Logger.d(TAG, "Focus duck")
                onDuck?.invoke(DUCK_VOLUME)
            }
        }
    }
}
