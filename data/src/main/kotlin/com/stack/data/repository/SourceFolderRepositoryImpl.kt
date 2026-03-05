package com.stack.data.repository

import com.stack.data.local.db.dao.SourceFolderDao
import com.stack.data.local.db.entity.SourceFolderEntity
import com.stack.domain.model.SourceFolder
import com.stack.domain.repository.SourceFolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SourceFolderRepositoryImpl @Inject constructor(
    private val sourceFolderDao: SourceFolderDao
) : SourceFolderRepository {

    override fun getAllFolders(): Flow<List<SourceFolder>> {
        return sourceFolderDao.getAllFolders().map { entities ->
            entities.map { entity ->
                SourceFolder(
                    id = entity.id,
                    treeUri = entity.treeUri,
                    displayName = entity.displayName,
                    addedAt = entity.addedAt,
                    lastScanAt = entity.lastScanAt
                )
            }
        }
    }

    override suspend fun getFolderById(id: Long): SourceFolder? {
        return sourceFolderDao.getFolderById(id)?.let { entity ->
            SourceFolder(
                id = entity.id,
                treeUri = entity.treeUri,
                displayName = entity.displayName,
                addedAt = entity.addedAt,
                lastScanAt = entity.lastScanAt
            )
        }
    }

    override suspend fun insertFolder(folder: SourceFolder): Long {
        return sourceFolderDao.insertFolder(
            SourceFolderEntity(
                id = folder.id,
                treeUri = folder.treeUri,
                displayName = folder.displayName,
                addedAt = folder.addedAt,
                lastScanAt = folder.lastScanAt
            )
        )
    }

    override suspend fun deleteFolder(id: Long) {
        sourceFolderDao.deleteFolder(id)
    }

    override suspend fun updateLastScanAt(id: Long, timestamp: Long) {
        sourceFolderDao.updateLastScanAt(id, timestamp)
    }

    override suspend fun getFolderCount(): Int {
        return sourceFolderDao.getFolderCount()
    }
}
