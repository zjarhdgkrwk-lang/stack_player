package com.stack.player.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.stack.core.player.PlaybackStateReducer
import com.stack.domain.model.enums.SystemTagType
import com.stack.domain.repository.TagRepository
import com.stack.player.MainActivity
import com.stack.player.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StackMediaService : MediaSessionService() {

    @Inject
    lateinit var tagRepository: TagRepository

    @Inject
    lateinit var playbackStateReducer: PlaybackStateReducer

    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val ACTION_TOGGLE_FAVORITE = "com.stack.player.TOGGLE_FAVORITE"
        val COMMAND_TOGGLE_FAVORITE = SessionCommand(ACTION_TOGGLE_FAVORITE, Bundle.EMPTY)
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val favoriteButton = CommandButton.Builder()
            .setDisplayName(getString(R.string.favorite))
            .setIconResId(R.drawable.ic_favorite_border)
            .setSessionCommand(COMMAND_TOGGLE_FAVORITE)
            .build()

        mediaSession = MediaSession.Builder(this, androidx.media3.exoplayer.ExoPlayer.Builder(this).build())
            .setSessionActivity(pendingIntent)
            .setCallback(FavoriteSessionCallback())
            .setCustomLayout(ImmutableList.of(favoriteButton))
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player ?: run {
            stopSelf()
            return
        }
        if (!player.playWhenReady) {
            stopSelf()
        }
    }

    @OptIn(UnstableApi::class)
    private inner class FavoriteSessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon()
                .add(COMMAND_TOGGLE_FAVORITE)
                .build()
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            if (customCommand.customAction == ACTION_TOGGLE_FAVORITE) {
                serviceScope.launch {
                    toggleFavoriteForCurrentTrack()
                }
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }
    }

    @OptIn(UnstableApi::class)
    private suspend fun toggleFavoriteForCurrentTrack() {
        val trackId = playbackStateReducer.state.value.currentTrack?.id ?: return
        val favoriteTag = tagRepository.getTagBySystemType(SystemTagType.FAVORITE) ?: return
        val isFavorite = tagRepository.isTrackTagged(trackId, favoriteTag.id)
        if (isFavorite) {
            tagRepository.removeTagFromTrack(trackId, favoriteTag.id)
        } else {
            tagRepository.assignTagToTrack(trackId, favoriteTag.id)
        }

        // Update the notification button icon
        val newButton = CommandButton.Builder()
            .setDisplayName(getString(R.string.favorite))
            .setIconResId(if (!isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
            .setSessionCommand(COMMAND_TOGGLE_FAVORITE)
            .build()
        mediaSession?.setCustomLayout(ImmutableList.of(newButton))
    }
}
