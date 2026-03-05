package com.stack.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.stack.data.local.db.entity.PlayHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayHistoryDao {

    @Query("SELECT * FROM play_history ORDER BY playedAt DESC LIMIT :limit")
    fun getRecentHistory(limit: Int): Flow<List<PlayHistoryEntity>>

    @Query("SELECT trackId FROM play_history GROUP BY trackId ORDER BY COUNT(*) DESC LIMIT :limit")
    fun getMostPlayedTrackIds(limit: Int): Flow<List<Long>>

    @Insert
    suspend fun insertPlayHistory(history: PlayHistoryEntity)

    @Query("DELETE FROM play_history")
    suspend fun clearHistory()

    @Query("SELECT COUNT(*) FROM play_history WHERE trackId = :trackId")
    suspend fun getPlayCountForTrack(trackId: Long): Int
}
