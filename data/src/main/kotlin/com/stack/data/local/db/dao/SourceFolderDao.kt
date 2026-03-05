package com.stack.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stack.data.local.db.entity.SourceFolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceFolderDao {

    @Query("SELECT * FROM source_folders ORDER BY addedAt DESC")
    fun getAllFolders(): Flow<List<SourceFolderEntity>>

    @Query("SELECT * FROM source_folders WHERE id = :id")
    suspend fun getFolderById(id: Long): SourceFolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: SourceFolderEntity): Long

    @Query("DELETE FROM source_folders WHERE id = :id")
    suspend fun deleteFolder(id: Long)

    @Query("UPDATE source_folders SET lastScanAt = :timestamp WHERE id = :id")
    suspend fun updateLastScanAt(id: Long, timestamp: Long)

    @Query("SELECT COUNT(*) FROM source_folders")
    suspend fun getFolderCount(): Int
}
