package com.stack.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.stack.data.local.db.entity.TrackEntity

@Dao
interface TrackFtsDao {

    @Query("""
        SELECT tracks.* FROM tracks
        JOIN tracks_fts ON tracks.rowid = tracks_fts.rowid
        WHERE tracks_fts MATCH :query
        AND tracks.status != 'DELETED'
    """)
    suspend fun search(query: String): List<TrackEntity>
}
