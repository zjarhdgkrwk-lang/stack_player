package com.stack.core.player

import com.stack.core.logging.Logger
import com.stack.domain.model.PlaybackSession
import com.stack.domain.model.Track
import com.stack.domain.model.enums.RepeatMode
import com.stack.domain.model.enums.ShuffleMode
import com.stack.domain.repository.PlaybackSessionRepository
import com.stack.domain.repository.TrackRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val sessionRepository: PlaybackSessionRepository,
    private val trackRepository: TrackRepository
) {
    companion object {
        private const val TAG = "SessionManager"
    }

    suspend fun saveSession(state: PlaybackState) {
        val track = state.currentTrack ?: return
        val queueJson = serializeQueue(state.queue)

        val session = PlaybackSession(
            lastTrackId = track.id,
            lastPositionMs = state.positionMs,
            lastQueueJson = queueJson,
            repeatMode = state.repeatMode,
            shuffleMode = state.shuffleMode,
            updatedAt = System.currentTimeMillis()
        )
        sessionRepository.saveSession(session)
        Logger.d(TAG, "Session saved: track=${track.id}, pos=${state.positionMs}")
    }

    suspend fun restoreSession(): RestoredSession? {
        val session = sessionRepository.getSession() ?: return null
        val trackId = session.lastTrackId ?: return null

        val track = trackRepository.getTrackById(trackId) ?: run {
            Logger.i(TAG, "Session track $trackId no longer in library, skipping restore")
            return null
        }

        val queue = deserializeQueue(session.lastQueueJson)

        return RestoredSession(
            track = track,
            queue = queue.ifEmpty { listOf(track) },
            positionMs = session.lastPositionMs,
            repeatMode = session.repeatMode,
            shuffleMode = session.shuffleMode
        )
    }

    suspend fun clearSession() {
        sessionRepository.clearSession()
    }

    private fun serializeQueue(tracks: List<Track>): String {
        return tracks.joinToString(",") { it.id.toString() }
    }

    private suspend fun deserializeQueue(json: String?): List<Track> {
        if (json.isNullOrBlank()) return emptyList()
        val ids = json.split(",").mapNotNull { it.trim().toLongOrNull() }
        return ids.mapNotNull { trackRepository.getTrackById(it) }
    }

    data class RestoredSession(
        val track: Track,
        val queue: List<Track>,
        val positionMs: Long,
        val repeatMode: RepeatMode,
        val shuffleMode: ShuffleMode
    )
}
