package com.stack.data.repository

import com.stack.data.local.db.dao.TrackDao
import com.stack.data.mapper.TrackMapper
import com.stack.domain.model.Track
import com.stack.domain.model.enums.SortOrder
import com.stack.domain.model.enums.TrackStatus
import com.stack.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackRepositoryImpl @Inject constructor(
    private val trackDao: TrackDao
) : TrackRepository {

    override fun getAllTracks(sortOrder: SortOrder): Flow<List<Track>> {
        val flow = when (sortOrder) {
            SortOrder.TITLE_ASC -> trackDao.getAllTracksByTitleAsc()
            SortOrder.TITLE_DESC -> trackDao.getAllTracksByTitleDesc()
            SortOrder.ARTIST_ASC -> trackDao.getAllTracksByArtistAsc()
            SortOrder.ARTIST_DESC -> trackDao.getAllTracksByArtistDesc()
            SortOrder.ALBUM_ASC -> trackDao.getAllTracksByAlbumAsc()
            SortOrder.ALBUM_DESC -> trackDao.getAllTracksByAlbumDesc()
            SortOrder.DATE_ADDED_ASC -> trackDao.getAllTracksByDateAddedAsc()
            SortOrder.DATE_ADDED_DESC -> trackDao.getAllTracksByDateAddedDesc()
            SortOrder.DURATION_ASC -> trackDao.getAllTracksByDurationAsc()
            SortOrder.DURATION_DESC -> trackDao.getAllTracksByDurationDesc()
            SortOrder.YEAR_ASC -> trackDao.getAllTracksByYearAsc()
            SortOrder.YEAR_DESC -> trackDao.getAllTracksByYearDesc()
        }
        return flow.map { TrackMapper.toDomainList(it) }
    }

    override fun getTracksByStatus(status: TrackStatus): Flow<List<Track>> {
        return trackDao.getTracksByStatus(status.name).map { TrackMapper.toDomainList(it) }
    }

    override fun getTracksByAlbumId(albumId: Long): Flow<List<Track>> {
        return trackDao.getTracksByAlbumId(albumId).map { TrackMapper.toDomainList(it) }
    }

    override fun getTracksByArtistId(artistId: Long): Flow<List<Track>> {
        return trackDao.getTracksByArtistId(artistId).map { TrackMapper.toDomainList(it) }
    }

    override fun getTracksByFolder(relativePath: String): Flow<List<Track>> {
        return trackDao.getTracksByFolder(relativePath).map { TrackMapper.toDomainList(it) }
    }

    override suspend fun getTrackById(id: Long): Track? {
        return trackDao.getTrackById(id)?.let { TrackMapper.toDomain(it) }
    }

    override suspend fun getTrackByContentUri(contentUri: String): Track? {
        return trackDao.getTrackByContentUri(contentUri)?.let { TrackMapper.toDomain(it) }
    }

    override suspend fun insertTrack(track: Track): Long {
        return trackDao.insertTrack(TrackMapper.toEntity(track))
    }

    override suspend fun insertTracks(tracks: List<Track>) {
        trackDao.insertTracks(tracks.map { TrackMapper.toEntity(it) })
    }

    override suspend fun updateTrack(track: Track) {
        trackDao.updateTrack(TrackMapper.toEntity(track))
    }

    override suspend fun deleteTrack(id: Long) {
        trackDao.deleteTrack(id)
    }

    override suspend fun updateTrackStatus(id: Long, status: TrackStatus) {
        trackDao.updateTrackStatus(id, status.name)
    }

    override suspend fun getTrackCount(): Int {
        return trackDao.getTrackCount()
    }
}
