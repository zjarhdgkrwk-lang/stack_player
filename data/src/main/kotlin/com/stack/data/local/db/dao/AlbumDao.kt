package com.stack.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stack.data.local.db.entity.AlbumEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    @Query("SELECT * FROM albums ORDER BY name ASC")
    fun getAllAlbumsByNameAsc(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums ORDER BY name DESC")
    fun getAllAlbumsByNameDesc(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums ORDER BY dateAdded ASC")
    fun getAllAlbumsByDateAddedAsc(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums ORDER BY dateAdded DESC")
    fun getAllAlbumsByDateAddedDesc(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums ORDER BY year ASC")
    fun getAllAlbumsByYearAsc(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums ORDER BY year DESC")
    fun getAllAlbumsByYearDesc(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE id = :id")
    suspend fun getAlbumById(id: Long): AlbumEntity?

    @Query("SELECT * FROM albums WHERE name = :name AND artist = :artist LIMIT 1")
    suspend fun getAlbumByNameAndArtist(name: String, artist: String?): AlbumEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity): Long

    @Update
    suspend fun updateAlbum(album: AlbumEntity)

    @Query("DELETE FROM albums WHERE id = :id")
    suspend fun deleteAlbum(id: Long)
}
