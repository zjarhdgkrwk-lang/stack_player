package com.stack.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stack.data.local.db.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Query("SELECT * FROM tracks WHERE status != 'DELETED' ORDER BY dateAdded DESC")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE status != 'DELETED' ORDER BY title ASC")
    fun getAllTracksByTitleAsc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE status != 'DELETED' ORDER BY title DESC")
    fun getAllTracksByTitleDesc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE status != 'DELETED' ORDER BY artist ASC")
    fun getAllTracksByArtistAsc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE status != 'DELETED' ORDER BY artist DESC")
    fun getAllTracksByArtistDesc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE status != 'DELETED' ORDER BY album ASC")
    fun getAllTracksByAlbumAsc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE status != 'DELETED' ORDER BY album DESC")
    fun getAllTracksByAlbumDesc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE status != 'DELETED' ORDER BY dateAdded ASC")
    fun getAllTracksByDateAddedAsc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE status != 'DELETED' ORDER BY dateAdded DESC")
    fun getAllTracksByDateAddedDesc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE status != 'DELETED' ORDER BY duration ASC")
    fun getAllTracksByDurationAsc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE status != 'DELETED' ORDER BY duration DESC")
    fun getAllTracksByDurationDesc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE status != 'DELETED' ORDER BY year ASC")
    fun getAllTracksByYearAsc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE status != 'DELETED' ORDER BY year DESC")
    fun getAllTracksByYearDesc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE status = :status")
    fun getTracksByStatus(status: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE albumId = :albumId AND status != 'DELETED' ORDER BY trackNumber ASC")
    fun getTracksByAlbumId(albumId: Long): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE artistId = :artistId AND status != 'DELETED' ORDER BY title ASC")
    fun getTracksByArtistId(artistId: Long): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE relativePath = :relativePath AND status != 'DELETED' ORDER BY fileName ASC")
    fun getTracksByFolder(relativePath: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun getTrackById(id: Long): TrackEntity?

    @Query("SELECT * FROM tracks WHERE contentUri = :contentUri")
    suspend fun getTrackByContentUri(contentUri: String): TrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Update
    suspend fun updateTrack(track: TrackEntity)

    @Query("DELETE FROM tracks WHERE id = :id")
    suspend fun deleteTrack(id: Long)

    @Query("UPDATE tracks SET status = :status WHERE id = :id")
    suspend fun updateTrackStatus(id: Long, status: String)

    @Query("SELECT COUNT(*) FROM tracks WHERE status != 'DELETED'")
    suspend fun getTrackCount(): Int
}
