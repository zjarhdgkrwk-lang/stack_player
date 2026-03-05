package com.stack.data.repository

import com.stack.data.local.db.dao.ArtistDao
import com.stack.data.mapper.ArtistMapper
import com.stack.domain.model.Artist
import com.stack.domain.model.enums.SortOrder
import com.stack.domain.repository.ArtistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtistRepositoryImpl @Inject constructor(
    private val artistDao: ArtistDao
) : ArtistRepository {

    override fun getAllArtists(sortOrder: SortOrder): Flow<List<Artist>> {
        val flow = when (sortOrder) {
            SortOrder.TITLE_ASC, SortOrder.ARTIST_ASC -> artistDao.getAllArtistsByNameAsc()
            SortOrder.TITLE_DESC, SortOrder.ARTIST_DESC -> artistDao.getAllArtistsByNameDesc()
            else -> artistDao.getAllArtistsByNameAsc()
        }
        return flow.map { ArtistMapper.toDomainList(it) }
    }

    override suspend fun getArtistById(id: Long): Artist? {
        return artistDao.getArtistById(id)?.let { ArtistMapper.toDomain(it) }
    }

    override suspend fun insertArtist(artist: Artist): Long {
        return artistDao.insertArtist(ArtistMapper.toEntity(artist))
    }

    override suspend fun updateArtist(artist: Artist) {
        artistDao.updateArtist(ArtistMapper.toEntity(artist))
    }

    override suspend fun deleteArtist(id: Long) {
        artistDao.deleteArtist(id)
    }
}
