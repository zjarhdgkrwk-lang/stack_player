package com.stack.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stack.data.local.db.entity.PlaylistEntity
import com.stack.data.local.db.entity.PlaylistTrackCrossRef
import com.stack.data.local.db.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("""
        SELECT tracks.* FROM tracks
        INNER JOIN playlist_track_cross_ref ON tracks.id = playlist_track_cross_ref.trackId
        WHERE playlist_track_cross_ref.playlistId = :playlistId
        ORDER BY playlist_track_cross_ref.orderIndex ASC
    """)
    fun getPlaylistTracks(playlistId: Long): Flow<List<TrackEntity>>

    @Query("""
        SELECT playlist_track_cross_ref.* FROM playlist_track_cross_ref
        WHERE playlist_track_cross_ref.playlistId = :playlistId
        ORDER BY playlist_track_cross_ref.orderIndex ASC
    """)
    fun getPlaylistTrackCrossRefs(playlistId: Long): Flow<List<PlaylistTrackCrossRef>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTrackCrossRef(crossRef: PlaylistTrackCrossRef)

    @Query("DELETE FROM playlist_track_cross_ref WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun deletePlaylistTrackCrossRef(playlistId: Long, trackId: Long)

    @Query("SELECT COALESCE(MAX(orderIndex), -1) FROM playlist_track_cross_ref WHERE playlistId = :playlistId")
    suspend fun getMaxOrderIndex(playlistId: Long): Int

    @Query("UPDATE playlist_track_cross_ref SET orderIndex = :orderIndex WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun updateOrderIndex(playlistId: Long, trackId: Long, orderIndex: Int)

    @Query("SELECT COUNT(*) FROM playlist_track_cross_ref WHERE playlistId = :playlistId")
    suspend fun getPlaylistTrackCount(playlistId: Long): Int

    @Query("SELECT COALESCE(SUM(tracks.duration), 0) FROM tracks INNER JOIN playlist_track_cross_ref ON tracks.id = playlist_track_cross_ref.trackId WHERE playlist_track_cross_ref.playlistId = :playlistId")
    suspend fun getPlaylistTotalDuration(playlistId: Long): Long
}
