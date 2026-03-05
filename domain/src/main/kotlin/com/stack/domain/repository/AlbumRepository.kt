package com.stack.domain.repository

import com.stack.domain.model.Album
import com.stack.domain.model.enums.SortOrder
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {
    fun getAllAlbums(sortOrder: SortOrder = SortOrder.TITLE_ASC): Flow<List<Album>>
    suspend fun getAlbumById(id: Long): Album?
    suspend fun insertAlbum(album: Album): Long
    suspend fun updateAlbum(album: Album)
    suspend fun deleteAlbum(id: Long)
}
