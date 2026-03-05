package com.stack.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stack.data.local.db.entity.PlaybackSessionEntity

@Dao
interface PlaybackSessionDao {

    @Query("SELECT * FROM playback_session WHERE id = 0")
    suspend fun getSession(): PlaybackSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: PlaybackSessionEntity)

    @Query("DELETE FROM playback_session")
    suspend fun clearSession()
}
