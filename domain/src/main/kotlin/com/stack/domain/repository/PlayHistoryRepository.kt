package com.stack.domain.repository

import com.stack.domain.model.PlayHistory
import kotlinx.coroutines.flow.Flow

interface PlayHistoryRepository {
    fun getRecentHistory(limit: Int = 100): Flow<List<PlayHistory>>
    fun getMostPlayedTrackIds(limit: Int = 100): Flow<List<Long>>
    suspend fun recordPlay(trackId: Long, durationPlayed: Long)
    suspend fun clearHistory()
    suspend fun getPlayCountForTrack(trackId: Long): Int
}
