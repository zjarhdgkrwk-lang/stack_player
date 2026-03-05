package com.stack.data.repository

import com.stack.data.local.db.dao.AlbumDao
import com.stack.data.mapper.AlbumMapper
import com.stack.domain.model.Album
import com.stack.domain.model.enums.SortOrder
import com.stack.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlbumRepositoryImpl @Inject constructor(
    private val albumDao: AlbumDao
) : AlbumRepository {

    override fun getAllAlbums(sortOrder: SortOrder): Flow<List<Album>> {
        val flow = when (sortOrder) {
            SortOrder.TITLE_ASC, SortOrder.ALBUM_ASC -> albumDao.getAllAlbumsByNameAsc()
            SortOrder.TITLE_DESC, SortOrder.ALBUM_DESC -> albumDao.getAllAlbumsByNameDesc()
            SortOrder.DATE_ADDED_ASC -> albumDao.getAllAlbumsByDateAddedAsc()
            SortOrder.DATE_ADDED_DESC -> albumDao.getAllAlbumsByDateAddedDesc()
            SortOrder.YEAR_ASC -> albumDao.getAllAlbumsByYearAsc()
            SortOrder.YEAR_DESC -> albumDao.getAllAlbumsByYearDesc()
            else -> albumDao.getAllAlbumsByNameAsc()
        }
        return flow.map { AlbumMapper.toDomainList(it) }
    }

    override suspend fun getAlbumById(id: Long): Album? {
        return albumDao.getAlbumById(id)?.let { AlbumMapper.toDomain(it) }
    }

    override suspend fun insertAlbum(album: Album): Long {
        return albumDao.insertAlbum(AlbumMapper.toEntity(album))
    }

    override suspend fun updateAlbum(album: Album) {
        albumDao.updateAlbum(AlbumMapper.toEntity(album))
    }

    override suspend fun deleteAlbum(id: Long) {
        albumDao.deleteAlbum(id)
    }
}
