package com.stack.data.repository

import com.stack.data.local.db.dao.PlaybackSessionDao
import com.stack.data.local.db.entity.PlaybackSessionEntity
import com.stack.domain.model.PlaybackSession
import com.stack.domain.model.enums.RepeatMode
import com.stack.domain.model.enums.ShuffleMode
import com.stack.domain.repository.PlaybackSessionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackSessionRepositoryImpl @Inject constructor(
    private val playbackSessionDao: PlaybackSessionDao
) : PlaybackSessionRepository {

    override suspend fun getSession(): PlaybackSession? {
        return playbackSessionDao.getSession()?.let { entity ->
            PlaybackSession(
                lastTrackId = entity.lastTrackId,
                lastPositionMs = entity.lastPositionMs,
                lastQueueJson = entity.lastQueueJson,
                repeatMode = RepeatMode.valueOf(entity.repeatMode),
                shuffleMode = ShuffleMode.valueOf(entity.shuffleMode),
                updatedAt = entity.updatedAt
            )
        }
    }

    override suspend fun saveSession(session: PlaybackSession) {
        playbackSessionDao.saveSession(
            PlaybackSessionEntity(
                id = 0,
                lastTrackId = session.lastTrackId,
                lastPositionMs = session.lastPositionMs,
                lastQueueJson = session.lastQueueJson,
                repeatMode = session.repeatMode.name,
                shuffleMode = session.shuffleMode.name,
                updatedAt = session.updatedAt
            )
        )
    }

    override suspend fun clearSession() {
        playbackSessionDao.clearSession()
    }
}
