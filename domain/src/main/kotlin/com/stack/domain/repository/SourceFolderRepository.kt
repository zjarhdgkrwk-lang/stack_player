package com.stack.domain.repository

import com.stack.domain.model.SourceFolder
import kotlinx.coroutines.flow.Flow

interface SourceFolderRepository {
    fun getAllFolders(): Flow<List<SourceFolder>>
    suspend fun getFolderById(id: Long): SourceFolder?
    suspend fun insertFolder(folder: SourceFolder): Long
    suspend fun deleteFolder(id: Long)
    suspend fun updateLastScanAt(id: Long, timestamp: Long)
    suspend fun getFolderCount(): Int
}
