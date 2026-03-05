package com.stack.core.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.stack.core.logging.Logger
import com.stack.domain.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StackPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "StackPlayerManager"
        private const val PRELOAD_THRESHOLD_MS = 10_000L
    }

    private var playerA: ExoPlayer? = null
    private var playerB: ExoPlayer? = null
    private var activePlayer: ExoPlayer? = null
    private var warmPlayer: ExoPlayer? = null
    private var commandDispatcher: CommandDispatcher? = null

    val audioSessionId: Int
        get() = activePlayer?.audioSessionId ?: 0

    fun initialize() {
        if (playerA != null) return
        playerA = createPlayer()
        playerB = createPlayer()
        activePlayer = playerA
        warmPlayer = playerB
        Logger.i(TAG, "Dual ExoPlayer initialized")
    }

    fun setCommandDispatcher(dispatcher: CommandDispatcher) {
        commandDispatcher = dispatcher
    }

    @OptIn(UnstableApi::class)
    private fun createPlayer(): ExoPlayer {
        return ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        // Will be routed through CommandDispatcher
                        Logger.d(TAG, "Track ended on player")
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Logger.e(TAG, "Player error: ${error.message}", error)
                }
            })
        }
    }

    suspend fun play(track: Track) {
        initialize()
        val player = activePlayer ?: return
        val uri = Uri.parse(track.contentUri)
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        Logger.d(TAG, "Playing: ${track.title}")
    }

    fun pause() {
        activePlayer?.pause()
    }

    fun resume() {
        activePlayer?.play()
    }

    fun seekTo(positionMs: Long) {
        activePlayer?.seekTo(positionMs)
    }

    fun stop() {
        activePlayer?.stop()
        activePlayer?.clearMediaItems()
        warmPlayer?.stop()
        warmPlayer?.clearMediaItems()
    }

    fun setPlaybackSpeed(speed: Float) {
        activePlayer?.setPlaybackSpeed(speed)
    }

    fun getCurrentPosition(): Long {
        return activePlayer?.currentPosition ?: 0L
    }

    fun getDuration(): Long {
        return activePlayer?.duration ?: 0L
    }

    fun preloadNext(track: Track) {
        val warm = warmPlayer ?: return
        val uri = Uri.parse(track.contentUri)
        warm.setMediaItem(MediaItem.fromUri(uri))
        warm.prepare()
        Logger.d(TAG, "Preloaded next track: ${track.title}")
    }

    fun swapPlayers() {
        val temp = activePlayer
        activePlayer = warmPlayer
        warmPlayer = temp
        warmPlayer?.stop()
        warmPlayer?.clearMediaItems()
    }

    fun setActiveVolume(volume: Float) {
        activePlayer?.volume = volume.coerceIn(0f, 1f)
    }

    fun setWarmVolume(volume: Float) {
        warmPlayer?.volume = volume.coerceIn(0f, 1f)
    }

    fun startWarmPlayer() {
        warmPlayer?.play()
    }

    fun release() {
        playerA?.release()
        playerB?.release()
        playerA = null
        playerB = null
        activePlayer = null
        warmPlayer = null
        Logger.i(TAG, "Players released")
    }
}
