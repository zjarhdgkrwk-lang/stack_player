package com.stack.data.repository

import com.stack.data.local.db.dao.PlayHistoryDao
import com.stack.data.local.db.entity.PlayHistoryEntity
import com.stack.domain.model.PlayHistory
import com.stack.domain.repository.PlayHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayHistoryRepositoryImpl @Inject constructor(
    private val playHistoryDao: PlayHistoryDao
) : PlayHistoryRepository {

    override fun getRecentHistory(limit: Int): Flow<List<PlayHistory>> {
        return playHistoryDao.getRecentHistory(limit).map { entities ->
            entities.map { entity ->
                PlayHistory(
                    id = entity.id,
                    trackId = entity.trackId,
                    playedAt = entity.playedAt,
                    durationPlayed = entity.durationPlayed
                )
            }
        }
    }

    override fun getMostPlayedTrackIds(limit: Int): Flow<List<Long>> {
        return playHistoryDao.getMostPlayedTrackIds(limit)
    }

    override suspend fun recordPlay(trackId: Long, durationPlayed: Long) {
        playHistoryDao.insertPlayHistory(
            PlayHistoryEntity(
                trackId = trackId,
                playedAt = System.currentTimeMillis(),
                durationPlayed = durationPlayed
            )
        )
    }

    override suspend fun clearHistory() {
        playHistoryDao.clearHistory()
    }

    override suspend fun getPlayCountForTrack(trackId: Long): Int {
        return playHistoryDao.getPlayCountForTrack(trackId)
    }
}
